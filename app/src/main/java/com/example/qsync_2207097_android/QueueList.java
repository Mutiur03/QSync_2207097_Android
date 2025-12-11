package com.example.qsync_2207097_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueList extends AppCompatActivity {
    private static final String TAG = "QueueList";

    RecyclerView recyclerView;
    ArrayList<WaitingList> items = new ArrayList<>();
    ListRVAdapter listRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_queue_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRVAdapter = new ListRVAdapter(items);
        recyclerView.setAdapter(listRVAdapter);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String json = null;
            try {
                URL url = new URL("https://raw.githubusercontent.com/Mutiur03/Emni/main/waiting_list.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = conn.getInputStream()) {
                        json = readStreamFully(in);
                    }
                }
                conn.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Network error while fetching waiting list", e);
            }

            final String finalJson = json;
            mainHandler.post(() -> {
                if (finalJson == null || finalJson.isEmpty()) {
                    populateWithSampleData();
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    WaitingList[] arr = mapper.readValue(finalJson, WaitingList[].class);
                    items.clear();
                    for (WaitingList w : arr) {
                        items.add(w);
                    }
                    listRVAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse waiting list JSON", e);
                    populateWithSampleData();
                }
            });
        });
    }

    private static String readStreamFully(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private void populateWithSampleData() {
        items.clear();
        for (int i = 0; i < 5; i++) {
            WaitingList w = new WaitingList(i + 1, "Sample " + (i + 1), (10 + i) + " minutes", "https://i.pravatar.cc/150?img=" + (i + 1));
            items.add(w);
        }
        listRVAdapter.notifyDataSetChanged();
    }
}