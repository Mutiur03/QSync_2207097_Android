package com.example.qsync_2207097_android;

public class Queue {
        public static class QueueEntry {
        public String queueId;
        public String patientId;
        public String doctorId;
        public int position;
        public long timestamp;
        public String status;
        public String today;
        public QueueEntry() {}

        public QueueEntry(String queueId, String patientId, String doctorId, int position, long timestamp, String status, String today) {
            this.queueId = queueId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.position = position;
            this.timestamp = timestamp;
            this.status = status;
            this.today = today;
        }
    }
}
