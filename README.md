# AAOS Annotation App

A real-time vehicle event logging and telematics data collection tool built for Android Automotive OS (AAOS). 

This application is designed to help drivers and engineers capture mission-critical events (like lane drifts, sensor anomalies, or sudden braking) during vehicle testing and integration milestones. It bridges the gap between raw vehicle signals and structured data collection.

## 🚀 What it does
*   **VHAL Integration**: Connects directly to the Android Automotive Vehicle Hardware Abstraction Layer (VHAL) to read real-time speed and vehicle signals via `CarPropertyManager`.
*   **Real-time Visualization**: Features a custom-built 3D canvas that visualizes the vehicle's surroundings, including detected cars and pedestrians.
*   **Event Logging (Annotations)**: Allows users to "Flag" specific moments with a single tap. Each event captures the exact speed, timestamp, and location.
*   **Local & Cloud Persistence**: Saves all logged events locally using a Room Database to ensure data isn't lost during drives, with a built-in repository pattern prepared for cloud synchronization.

## 🛠 Tech Stack
*   **Language**: Kotlin
*   **UI**: Jetpack Compose (Modern, declarative UI)
*   **Architecture**: MVVM (Model-View-ViewModel) for clean separation of concerns.
*   **Database**: Room (SQLite) for robust local storage.
*   **Hardware**: Android Automotive OS API (android.car).

## 📁 Project Structure
*   **UINEW**: Contains the Compose-based UI and the custom 3D visualization canvas.
*   **viewmodel**: Manages the application state and coordinates between the VHAL and the UI.
*   **mock**: Includes the logic for VHAL connection and a fallback system for testing on non-automotive devices (phones).
*   **repository**: Handles data operations between the local database and the cloud logic.

## 🚦 Getting Started
1. Clone the repository.
2. Open in Android Studio (Koala or later).
3. Deploy to an Android Automotive Emulator or a compatible AAOS head unit.
4. (Optional) Run on a standard Android phone to test the UI using the built-in mock signal system.

---
*Built for the future of connected vehicle telematics.*
