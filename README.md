# SOEN-345-Project

## Overview
This is a university class project developed for SOEN 345 using Android Studio. It is an event scheduling and reservation application that allows administrators to create and manage events, and users to view and book reservations for these events. The project features role-based access control, integrates Firebase for backend services (Authentication, Firestore Database), and utilizes EmailJS for background email notifications regarding event registrations, cancellations, and cascading event deletions by the administrator.

## How to Set Up the Project
1. **Prerequisites**: Ensure you have [Android Studio](https://developer.android.com/studio) installed on your machine.
2. **Clone the repository**:
   ```bash
   git clone <repository_url>
   cd SOEN-345-Project
   ```
3. **Firebase Setup**: The project relies on Firebase services. Ensure that the `google-services.json` file is correctly placed in the `app/` directory (it is included in the project structure) to allow connection to the Firestore Database and Authentication services.

## How to Run the App (Using Android Studio)
1. Open **Android Studio**.
2. Select **File > Open** from the menu, navigate to the directory where you cloned the repository (`SOEN-345-Project`), and click **OK**.
3. Wait for Android Studio to sync the project with the Gradle build scripts. This might take a few moments.
4. Once the Gradle sync is complete and successful, select a target deployment device from the device drop-down menu in the top toolbar. You can either use an **Android Virtual Device (Emulator)** (create one from the Device Manager if you haven't already) or connect a physical Android device via USB/Wi-Fi.
5. Click the **Run 'app'** button (the green play icon) in the toolbar, or press `Shift + F10`.
6. Android Studio will build the project and launch the application on your selected device or emulator.

## How to Run Tests
The project contains comprehensive test suites, including unit tests, component tests, and system acceptance tests.

### Run All Local Tests (Unit & Acceptance)
To run all tests (which do not require a device emulator), use the following Gradle command in your terminal:
```bash
./gradlew testDebugUnitTest
```
This will execute all testing domains (Login, Register, Dashboard ViewModels) along with the acceptance tests. You can view the test report at `app/build/reports/tests/testDebugUnitTest/index.html`.

### Run ONLY the Acceptance Tests
If you want to evaluate specifically the end-to-end functional and acceptance tests mapping to the project requirements:
```bash
./gradlew testDebugUnitTest --tests "com.team_one.soen_345_project.acceptance.*"
```

### Run UI / Instrumented Tests
To run the Espresso UI layout and navigation tests (an Android Emulator or physical device MUST be running):
```bash
./gradlew connectedAndroidTest
```
