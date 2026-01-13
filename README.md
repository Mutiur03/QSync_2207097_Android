# QSync - Queue Management System (Android)

**Student ID:** 2207097

## 📋 Project Description

QSync is a comprehensive Android-based queue management system designed for healthcare facilities to streamline patient appointment scheduling and queue management. The application provides a dual-interface solution with separate portals for patients and administrators, enabling efficient management of departments, doctors, and patient queues in real-time.

### Key Features

- **Dual User Interface**: Separate portals for patients and administrators
- **Real-time Queue Management**: Live tracking of patient queues across departments
- **Firebase Integration**: Secure authentication and real-time database synchronization
- **Department & Doctor Management**: Admin tools to manage healthcare providers
- **Priority-based Queuing**: Support for normal, urgent, and emergency appointments
- **User Profiles**: Personalized user accounts with appointment history
- **Material Design UI**: Modern, intuitive interface following Material Design principles

---

## 🚀 Work Completed So Far

### ✅ Authentication System

- **User Login & Registration**: Email/password authentication via Firebase Auth
- **Admin Login**: Separate admin authentication portal with role-based access control
- **Auto-login**: Automatic session management with token refresh
- **Role Detection**: Automatic routing based on user role (admin/patient)

### ✅ User Portal

Implemented a complete patient-facing interface with bottom navigation:

- **Home Fragment**: Dashboard displaying available departments and doctors
- **Join Fragment**:
  - Department and doctor selection
  - Priority level selection (Normal, Urgent, Emergency)
  - Symptoms/reason for visit input
  - Estimated wait time calculation
  - Queue position preview
- **History Fragment**: View past appointments and queue history
- **Profile Fragment**: User profile management and settings

### ✅ Admin Portal

Comprehensive administrative dashboard with:

- **Admin Home**:
  - Live queue monitoring with expandable tree view
  - Real-time status updates (Waiting, In Progress, Completed, Cancelled)
  - Department and doctor-wise queue organization
- **Manage Fragment**: Central hub for administrative tasks
  - Department management (Add, Edit, Delete)
  - Doctor management (Add, Edit, Delete with department assignment)
- **Admin Profile**: Admin account settings and logout functionality
- **Completed Fragment**: Archive of completed appointments

### ✅ Data Models

- **User**: Patient information model
- **Department**: Healthcare department structure
- **Doctor**: Doctor profiles with department associations
- **Queue**: Appointment queue items with status tracking
- **AdminQueueItem**: Enhanced queue model for admin view

### ✅ UI Components

- **Custom Adapters**:
  - `DepartmentAdapter`: Display departments in RecyclerView
  - `DoctorAdapter`: Display doctors with specialty information
  - `QueueAdapter`: Patient queue list management
  - `AdminExpandableAdapter`: Hierarchical queue view for admins
  - `ManageDoctorAdapter`: Doctor management interface
- **Material Design Elements**:
  - Custom drawables (gradients, backgrounds, icons)
  - Bottom navigation bars
  - Floating action buttons
  - Dialog components
  - Card layouts with elevation

### ✅ Firebase Integration

- **Firebase Authentication**: Secure user and admin authentication
- **Firebase Realtime Database**:
  - User data storage
  - Department and doctor information
  - Queue management
  - Admin role verification
- **Firebase Firestore**: Additional data persistence (configured)

### ✅ Technical Implementation

- **ViewBinding**: Type-safe view access throughout the application
- **Fragment Navigation**: Smooth transitions between app sections
- **Edge-to-Edge Display**: Modern Android UI with system bar handling
- **Night Mode Disabled**: Consistent light theme experience
- **Input Validation**: Email pattern matching and password requirements
- **Error Handling**: Comprehensive error messages and user feedback

---

## 🛠️ Technology Stack

- **Language**: Java
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Build System**: Gradle (Kotlin DSL)
- **Architecture**: Fragment-based navigation with MVVM patterns

### Dependencies

- AndroidX AppCompat, Material Components, ConstraintLayout
- Firebase Authentication, Realtime Database, Firestore
- Google Play Services Auth
- RecyclerView for list displays
- ViewBinding for UI interaction

---

## 📱 Application Structure

```
QSync Android App
├── Authentication
│   ├── MainActivity (User Login)
│   ├── UserReg (User Registration)
│   └── AdminLogin (Admin Portal)
├── User Portal (Menu Activity)
│   ├── HomeFragment
│   ├── JoinFragment
│   ├── HistoryFragment
│   └── ProfileFragment
└── Admin Portal (AdminDashboard)
    ├── AdminHomeFragment
    ├── ManageFragment
    │   ├── ManageDepartmentsActivity
    │   └── ManageDoctorsActivity
    ├── AdminCompletedFragment
    └── AdminProfileFragment
```

---

## 🎯 Current Status

The application has a fully functional foundation with:

- ✅ Complete authentication flow
- ✅ User and admin interfaces implemented
- ✅ Queue management system operational
- ✅ Department and doctor management tools
- ✅ Real-time Firebase synchronization
- ✅ Material Design UI components

### Future Enhancements (Potential)

- Push notifications for queue updates
- Analytics dashboard for admins
- Appointment scheduling in advance
- Multi-language support
- Offline mode capabilities
- Patient feedback system

---

## 📄 License

This is an academic project developed as part of coursework (Student ID: 2207097).

---

## 👨‍💻 Developer

**Student ID**: 2207097  
**Project**: QSync - Queue Management System for Healthcare Facilities
