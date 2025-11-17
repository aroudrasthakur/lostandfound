# UTA Lost & Found - Android App

A comprehensive Lost & Found Android application built with Kotlin, Jetpack Compose, and Firebase for the UTA campus community.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## ⚠️ Important - Required Files

**Before building this project**, you must add the following files that are **NOT included** in the repository:

1. **`app/google-services.json`** (Required) - Firebase configuration file
   - Download from [Firebase Console](https://console.firebase.google.com/)
   - See [Firebase Setup](#firebase-setup) section below
2. **Google Maps API Key** (Optional) - For location autocomplete
   - Add to `app/src/main/res/values/strings.xml`
   - Replace `YOUR_API_KEY_HERE` with your actual key
   - See [Google Places API Setup](#google-places-api-setup) section below
   - Falls back to mock UTA locations if not configured

---

## 📑 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Firebase Setup](#firebase-setup)
  - [Google Places API Setup](#google-places-api-setup)
- [Running the App](#-running-the-app)
- [Adding Mock Data](#-adding-mock-data)
- [Cloud Functions Deployment](#%EF%B8%8F-cloud-functions-deployment)
- [Testing Guide](#-testing-guide)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

---

## ✨ Features

### Core Functionality

- ✅ **User Authentication** - Email/password authentication with Firebase
- ✅ **Report Lost Items** - Report items with photos, descriptions, categories, and locations
- ✅ **Report Found Items** - Add discovered items to help reunite with owners
- ✅ **Smart Search** - Search items by query, category, or location
- ✅ **Auto-Matching** - Cloud Functions automatically match lost and found items
- ✅ **Push Notifications** - Get notified when potential matches are found
- ✅ **Location Autocomplete** - Google Places API integration with UTA campus focus
- ✅ **Admin Dashboard** - View metrics and moderate reported items
- ✅ **Modern UI** - Clean Material 3 design with minimalistic modern theme

### Recent Updates (November 2025)

- 📸 **Camera Integration** - Take photos directly from the app with FileProvider support
- 🔄 **Manual Refresh** - Refresh button on Lost/Found tabs to reload data
- 🖼️ **Image Placeholders** - Graceful handling for items without uploaded images
- 🎨 **Content-Specific Images** - Real Unsplash images matching item descriptions
- 🗂️ **Complete Data Models** - Full toMap() implementations for all Firebase entities
- 📱 **Enhanced UI** - Info icon placeholders for missing images
- 🛠️ **Bug Fixes** - Enum serialization fixed, both lost and found items now load correctly

### Technical Features

- 🎨 **Material 3 Design** - Modern, minimalistic UI with custom color scheme
- 🏗️ **MVVM Architecture** - Clean separation of concerns
- 🔄 **Real-time Updates** - Firestore listeners for instant data sync
- 📸 **Multiple Image Upload** - Support for multiple photos per item with camera and gallery
- 🌍 **Location Validation** - Dropdown with 5 best matching locations
- 📊 **Analytics Dashboard** - Monthly statistics for admins
- 🔐 **Secure** - Firebase Authentication and Firestore security rules
- 📦 **Mock Data Seeder** - Testing utility with 18+ sample items

---

## 📝 Recent Changes

### November 2025 Updates

**Camera Integration**

- Added "Take Photo" button functionality with FileProvider support
- Temporary image files stored in cache directory
- Camera launcher integrated with TakePicture contract
- Added `file_paths.xml` configuration for secure file sharing

**Data Model Enhancements**

- Fixed ItemStatus enum serialization (uppercase LOST/FOUND)
- Completed toMap() implementations for all data models (Item, User, Match, Metrics)
- Added full repository method coverage

**UI/UX Improvements**

- Added placeholder images for items without photos (Info icon)
- Integrated 18 content-specific Unsplash images for mock data
- Added manual refresh button (FAB) on Lost/Found tabs
- Fixed items visibility bug (now loads both lost and found items)
- Enhanced image display with conditional rendering

**Documentation**

- Created `requirements.txt` for dependency reference
- Updated README with two-option Firebase setup (own project vs shared access)
- Added Firebase collaboration workflow documentation
- Comprehensive `.gitignore` with security exclusions

---

## 📱 Screenshots

_Coming soon - Install the app to see it in action!_

---

## 🛠️ Technology Stack

### Android App

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Repository Pattern
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Build Tool**: Gradle 8.2.0
- **JDK**: Java 17

### Backend Services

- **Firebase Authentication** - Email/password authentication
- **Cloud Firestore** - NoSQL database for items, users, matches
- **Cloud Storage** - Image storage for item photos
- **Cloud Messaging (FCM)** - Push notifications
- **Cloud Functions** - Auto-matching logic (Node.js 18)

### Key Libraries

```kotlin
// Jetpack Compose
androidx.compose.material3:material3
androidx.navigation:navigation-compose:2.7.6

// Firebase
com.google.firebase:firebase-bom:32.7.0
firebase-auth-ktx, firebase-firestore-ktx, firebase-storage-ktx, firebase-messaging-ktx

// Google Services
com.google.android.libraries.places:places:3.3.0

// Image Loading
io.coil-kt:coil-compose:2.5.0

// Coroutines
kotlinx-coroutines-android:1.7.3
```

---

## 🏛️ Project Architecture

### MVVM Pattern

```
UI Layer (Compose Screens)
    ↓
ViewModel Layer (Business Logic)
    ↓
Repository Layer (Data Operations)
    ↓
Data Sources (Firebase, Local)
```

### Package Structure

```
com.uta.lostfound/
├── data/
│   ├── model/          # Data models (User, Item, Match, Metrics)
│   ├── repository/     # Data access layer
│   └── service/        # Location autocomplete service
├── viewmodel/          # ViewModels for each screen
├── firebase/           # Firebase configuration & services
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   └── theme/          # Material 3 theme (Color, Type, Theme)
└── utils/              # Utilities (Data seeder, API validator)
```

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio** - Latest version (Hedgehog or newer)
- **JDK 17** - Java Development Kit
- **Android SDK** - API 24 to API 34
- **Git** - Version control
- **Firebase Account** - Free tier is sufficient
- **Google Cloud Account** - For Places API (optional, has fallback)

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/aroudrasthakur/lostandfound.git
   cd lostandfound
   ```

2. **Open in Android Studio**

   - Launch Android Studio
   - File → Open → Select the `lostandfound` folder
   - Wait for Gradle sync to complete

3. **⚠️ IMPORTANT: Add Required Files**

   The following files are **NOT included** in the repository for security reasons and **must be added manually**:

   #### a) `google-services.json` (Required)

   - Download from Firebase Console (see [Firebase Setup](#firebase-setup) below)
   - Place at: `app/google-services.json`
   - **Without this file, the app will not compile**

   #### b) Google Maps API Key (Optional but Recommended)

   - Get from Google Cloud Console (see [Google Places API Setup](#google-places-api-setup) below)
   - Add to: `app/src/main/res/values/strings.xml`
   - Replace: `<string name="google_maps_key">YOUR_API_KEY_HERE</string>`
   - **Without this, location autocomplete falls back to mock UTA campus locations**

4. **Build the project**

   ```powershell
   .\gradlew assembleDebug
   ```

   If build fails with "google-services.json not found", complete Firebase Setup first.

5. **Dependencies Reference (Optional)**

   A `requirements.txt` file is included in the repository as a **reference** for all project dependencies. Note that Android projects use Gradle (not pip), so this file is for documentation purposes only. All actual dependencies are automatically downloaded by Gradle during the build process.

### Firebase Setup

**IMPORTANT:** You have two options for Firebase setup:

#### Option 1: Use Your Own Firebase Project (Recommended for Development)

Follow these steps to create your own independent Firebase backend:

#### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Create a project"** or **"Add project"**
3. Enter a project name (e.g., "uta-lostandfound-yourname")
4. Accept terms and click **"Continue"**
5. Disable Google Analytics (optional) or configure it
6. Click **"Create project"**

#### Step 2: Add Android App

1. In Firebase Console, click gear icon ⚙️ → **Project Settings**
2. Scroll to **"Your apps"** section
3. Click **"Add app"** → Select **Android** 📱
4. Enter the following:
   - **Android package name**: `com.uta.lostfound`
   - **App nickname**: UTA Lost & Found (or your custom name)
   - **Debug SHA-1**: (optional for now)
5. Click **"Register app"**

#### Step 3: Download Configuration

1. **Download** the `google-services.json` file
2. Place it in: `app/google-services.json` (at project root level, same as `build.gradle.kts`)
3. In `app/build.gradle.kts`, ensure this line is **uncommented**:
   ```kotlin
   id("com.google.gms.google-services")
   ```

#### Step 4: Enable Firebase Services

In Firebase Console for your project:

1. **Authentication**

   - Navigate to **Build** → **Authentication**
   - Click **"Get started"**
   - Select **"Email/Password"** sign-in method
   - Enable it and Save

2. **Firestore Database**

   - Navigate to **Build** → **Firestore Database**
   - Click **"Create database"**
   - Select **"Start in production mode"**
   - Choose a location (closest to your users)
   - Click **"Enable"**

3. **Cloud Storage**

   - Navigate to **Build** → **Storage**
   - Click **"Get started"**
   - Start in **production mode**
   - Choose same location as Firestore
   - Click **"Done"**

4. **Cloud Messaging (FCM)**
   - Automatically enabled when you add Android app

#### Step 5: Configure Firestore Security Rules

In Firestore Database → **Rules** tab, replace with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /lost_items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null &&
        resource.data.userId == request.auth.uid;
    }
    match /found_items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null &&
        resource.data.userId == request.auth.uid;
    }
    match /matches/{matchId} {
      allow read: if request.auth != null;
    }
  }
}
```

Click **"Publish"** to save the rules.

#### Step 6: Configure Storage Security Rules

In Cloud Storage → **Rules** tab, replace with:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

Click **"Publish"** to save the rules.

#### Step 7: Rebuild

```powershell
.\gradlew clean assembleDebug
```

---

#### Option 2: Request Access to Existing Firebase Project

If you want to collaborate using the existing Firebase backend:

1. **Contact the Project Owner**

   - Reach out to the repository owner
   - Provide your Google account email (Gmail recommended)

2. **Owner Adds You as Collaborator**

   - Owner goes to Firebase Console → Project Settings
   - Navigates to **Users and permissions**
   - Clicks **"Add member"**
   - Enters your email and assigns role:
     - **Viewer**: Read-only access to view data
     - **Editor**: Can modify data and settings
     - **Owner**: Full access (rarely needed)

3. **Access Firebase Console**

   - Check your email for Firebase invitation
   - Accept the invitation
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select the project: **lostandfound-df6c2** (or project name)

4. **Download google-services.json**

   - In Firebase Console → Project Settings
   - Scroll to **"Your apps"**
   - Find the Android app (`com.uta.lostfound`)
   - Click the app, then download `google-services.json`
   - Place at: `app/google-services.json`

5. **Rebuild**
   ```powershell
   .\gradlew clean assembleDebug
   ```

**Note:** You'll be using the same Firestore database, Storage, and Authentication as other collaborators. Any data you add will be visible to all team members.

---

### Google Places API Setup

The app uses Google Places API for location autocomplete. If not configured, it falls back to mock UTA campus locations.

#### Step 1: Get API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select a project
3. Enable **Places API**
4. Go to **Credentials** → **Create Credentials** → **API Key**
5. Copy the API key

#### Step 2: Configure Restrictions (Recommended)

1. Edit the API key
2. Under **Application restrictions**, select **Android apps**
3. Add package: `com.uta.lostfound`
4. Get SHA-1 fingerprint:
   ```powershell
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
5. Add SHA-1 to restrictions
6. Under **API restrictions**, select **Places API**

#### Step 3: Enable Billing

- Google Places requires billing enabled (offers $200 free monthly credit)
- Link a billing account in Google Cloud Console

#### Step 4: Add to App

1. Open `app/src/main/res/values/strings.xml`
2. Replace the API key:
   ```xml
   <string name="google_maps_key">YOUR_ACTUAL_API_KEY_HERE</string>
   ```

#### Fallback Mode

If API key is not configured or fails, the app automatically uses **mock UTA campus locations**:

- Central Library
- Engineering Research Building
- University Center
- Science Hall
- College Park Center
- And more...

---

## 🏃 Running the App

### Option 1: Using Android Studio (Recommended)

1. **Create an Emulator** (if you don't have one)

   - Tools → Device Manager → Create Device
   - Select: Pixel 5 or Pixel 6
   - System Image: Android 13 (API 33)
   - Click Finish

2. **Start the Emulator**

   - Click Play ▶️ button next to your device

3. **Run the App**
   - Click green Run button ▶️ in toolbar
   - Or press `Shift + F10`
   - Select your emulator

### Option 2: Command Line

```powershell
# Build and install
.\gradlew installDebug

# Launch app
adb shell am start -n com.uta.lostfound/.ui.MainActivity
```

---

## 📦 Adding Mock Data

To test the app with sample data, use the built-in data seeder:

### Using the App

1. **Install the app**

   ```powershell
   .\gradlew installDebug
   ```

2. **Sign up/Login** to the app

3. **Go to Profile Tab** (bottom navigation, person icon)

4. **In "Development Tools" section**:

   - Click **"Add Mock Data"** → Adds 8 lost + 10 found items
   - Click **"Clear Data"** → Removes all items

5. **Navigate to Lost/Found tabs** to see the data

### What Gets Added

#### 8 Lost Items:

- Black Backpack (Central Library)
- iPhone 13 Pro (Engineering Building)
- Car Keys with Honda Keychain (University Center)
- Blue Denim Jacket (Science Hall)
- Calculus Textbook (Nedderman Hall)
- AirPods Pro (Rec Center)
- Student ID Card (College Park Center)
- Silver Watch (Fine Arts Building)

#### 10 Found Items:

- Red Water Bottle
- Wireless Mouse
- Blue Umbrella
- Black Glasses
- Notebook
- Gray Hoodie
- Set of Keys
- Graphing Calculator
- Black Wallet
- USB Flash Drive

All items have:

- ✅ Realistic descriptions
- ✅ UTA campus locations
- ✅ Timestamps (2 hours to 7 days ago)
- ✅ Multiple categories
- ✅ Mock user names

---

## ☁️ Cloud Functions Deployment

Auto-matching functionality requires deploying Cloud Functions:

1. **Install Firebase CLI**

   ```powershell
   npm install -g firebase-tools
   ```

2. **Login to Firebase**

   ```powershell
   firebase login
   ```

3. **Navigate to functions directory**

   ```powershell
   cd cloud_functions
   ```

4. **Initialize (if needed)**

   ```powershell
   firebase init functions
   ```

   - Select project: **lostandfound-df6c2**
   - Language: JavaScript
   - Use existing files

5. **Deploy**

   ```powershell
   firebase deploy --only functions
   ```

6. **Verify deployment**
   - Check Firebase Console → Functions
   - Should see: `onItemCreated`

---

## 🧪 Testing Guide

### Feature Checklist

#### 1. Authentication

- [ ] Sign up with email/password
- [ ] Login with credentials
- [ ] View profile information
- [ ] Logout successfully

#### 2. Report Items

- [ ] Click "+" button in top-right navbar
- [ ] Select "Report Lost Item"
- [ ] Fill all fields:
  - Title
  - Description (multi-line)
  - Location (with autocomplete dropdown)
  - Category (dropdown with all categories)
  - Date
  - Photos (multiple)
- [ ] Submit successfully
- [ ] View item in Lost Items tab

#### 3. Search & Browse

- [ ] Browse Lost Items tab
- [ ] Browse Found Items tab (CheckCircle icon)
- [ ] Tap on an item to view details
- [ ] Test search functionality

#### 4. Location Autocomplete

- [ ] Start typing in location field
- [ ] See dropdown with 5 suggestions
- [ ] Select a location
- [ ] See ✓ checkmark for valid location
- [ ] Try submitting without selection (should show error)

#### 5. Mock Data

- [ ] Go to Profile tab
- [ ] Click "Add Mock Data"
- [ ] See success message
- [ ] Verify items appear in Lost/Found tabs
- [ ] Click "Clear Data"
- [ ] Verify items are removed

#### 6. Admin Features (if admin role)

- [ ] See admin badge on Profile
- [ ] Access admin dashboard
- [ ] View metrics
- [ ] Moderate items

---

## 📂 Project Structure

```
lostandfound/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/uta/lostfound/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Item.kt
│   │   │   │   │   │   ├── Match.kt
│   │   │   │   │   │   └── Metrics.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   ├── ItemRepository.kt
│   │   │   │   │   │   ├── SearchRepository.kt
│   │   │   │   │   │   ├── MetricsRepository.kt
│   │   │   │   │   │   └── NotificationRepository.kt
│   │   │   │   │   └── service/
│   │   │   │   │       └── LocationAutocompleteService.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── LoginViewModel.kt
│   │   │   │   │   ├── ReportViewModel.kt
│   │   │   │   │   ├── FoundItemsViewModel.kt
│   │   │   │   │   ├── SearchViewModel.kt
│   │   │   │   │   ├── ItemDetailsViewModel.kt
│   │   │   │   │   └── AdminViewModel.kt
│   │   │   │   ├── firebase/
│   │   │   │   │   ├── FirebaseModule.kt
│   │   │   │   │   └── NotificationService.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── SignUpScreen.kt
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── ReportItemScreen.kt
│   │   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   │   ├── ItemDetailsScreen.kt
│   │   │   │   │   │   ├── AdminDashboardScreen.kt
│   │   │   │   │   │   └── AdminModerationScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   └── LocationAutocompleteField.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   └── Theme.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── NavGraph.kt
│   │   │   │   │   └── MainActivity.kt
│   │   │   │   └── utils/
│   │   │   │       ├── FirebaseDataSeeder.kt
│   │   │   │       └── ApiKeyValidator.kt
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml           # App strings and API keys
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── drawable/
│   │   │   │   └── xml/
│   │   │   │       └── file_paths.xml        # FileProvider config for camera
│   │   │   └── AndroidManifest.xml           # App permissions and services
│   │   └── build.gradle.kts
│   └── google-services.json                  # ⚠️ NOT IN REPO - Add manually
├── cloud_functions/
│   ├── index.js                              # Auto-matching logic
│   └── package.json                          # Node.js dependencies
├── requirements.txt                          # 📖 Dependencies reference
├── .gitignore                                # Security exclusions
└── README.md                                 # This file
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🗄️ Database Schema

### Firestore Collections

#### `users`

```javascript
{
  id: string,
  name: string,
  email: string,
  role: string,          // "user" or "admin"
  fcmToken: string,      // For push notifications
  createdAt: timestamp,
  updatedAt: timestamp
}
```

#### `lost_items`

```javascript
{
  id: string,
  title: string,
  description: string,
  category: string,      // ItemCategory enum
  location: string,
  date: timestamp,
  imageUrl: string,
  userId: string,
  userName: string,
  status: string,        // "lost"
  isActive: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

#### `found_items`

```javascript
{
  id: string,
  title: string,
  description: string,
  category: string,      // ItemCategory enum
  location: string,
  date: timestamp,
  imageUrl: string,
  userId: string,
  userName: string,
  status: string,        // "found"
  isActive: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

#### `matches`

```javascript
{
  id: string,
  lostItemId: string,
  foundItemId: string,
  similarityScore: number,
  matchedAt: timestamp,
  notifiedUsers: array<string>
}
```

### Categories

```kotlin
enum class ItemCategory {
    ELECTRONICS,    // Phones, laptops, headphones
    CLOTHING,       // Jackets, shirts, shoes
    ACCESSORIES,    // Watches, jewelry, glasses
    BOOKS,          // Textbooks, notebooks
    KEYS,           // Car keys, room keys
    BAGS,           // Backpacks, purses
    DOCUMENTS,      // IDs, passports, papers
    OTHER           // Anything else
}
```

---

## 🔧 Troubleshooting

### Build Errors

#### "google-services.json is missing"

**Solution:**

- Download `google-services.json` from Firebase Console
- Place in `app/` directory (same level as `build.gradle.kts`)
- Uncomment `id("com.google.gms.google-services")` in `app/build.gradle.kts` if commented
- Rebuild project

**If using existing Firebase project:**

- Request access from project owner
- Download config file after being granted access

#### "Execution failed for task ':app:compileDebugKotlin'"

```powershell
.\gradlew clean
.\gradlew assembleDebug
```

#### "Duplicate class found"

- Check for conflicting dependencies
- Sync Gradle files

### Firebase Issues

#### "Permission Denied" when reading Firestore

- Check Firestore Rules in Firebase Console
- Ensure user is authenticated
- Verify Auth is enabled in Firebase Console
- **If using shared Firebase:** Confirm you have Editor/Viewer access from project owner

#### "API key not valid" for Google Places

- Check API key in `app/src/main/res/values/strings.xml`
- Verify Places API is enabled in Google Cloud Console
- Ensure billing is enabled in Google Cloud
- Check API key restrictions match your package name

#### Cannot access Firebase Console

- Verify you've been added as a collaborator (check email for invitation)
- Ensure you're logged in with the correct Google account
- Contact project owner to verify your access level

### App Runtime Issues

#### Location dropdown not appearing

- Check Logcat for errors (filter: "LocationAutocomplete")
- Should fallback to mock UTA campus data if API fails
- Verify internet connection
- Confirm API key is correctly configured

#### Items not loading

- Check Firebase configuration (`google-services.json` present)
- Verify user is logged in
- Check Firestore data exists (use "Add Mock Data" in Profile tab)
- Review Logcat for errors
- **If using shared Firebase:** Verify data exists and rules allow reading

#### Images not uploading

- Check Storage rules in Firebase Console
- Verify camera/storage permissions in AndroidManifest
- Check file size limits
- Ensure Storage is enabled in Firebase project

### Setup Checklist

If you cloned the repo and app won't build/run, verify:

- [ ] `google-services.json` exists in `app/` directory
- [ ] Firebase Authentication is enabled (Email/Password)
- [ ] Firestore Database is created
- [ ] Cloud Storage is set up
- [ ] Google Places API key is added (or accepting mock data fallback)
- [ ] Android SDK is installed (API 24-34)
- [ ] JDK 17 is configured
- [ ] Gradle sync completed successfully

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Notes

- This repo uses `.gitignore` to exclude `google-services.json` and API keys for security
- Each contributor must set up their own Firebase project and configuration files (see Firebase Setup)
- Mock data is available for testing without real user data
- **Dependencies:** All Android dependencies are managed via Gradle. The `requirements.txt` file is included as a reference document only - Gradle automatically handles all downloads during build.
- Test your changes with both camera and gallery image uploads
- Verify that placeholder images display correctly for items without images
- Use the "Add Mock Data" feature in the Profile tab for testing

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Authors

- **Aroudra S Thakur** - [GitHub](https://github.com/aroudrasthakur)

---

## 🙏 Acknowledgments

- UTA Community for inspiration
- Firebase for backend infrastructure
- Google Places API for location services
- Material Design team for UI guidelines
- Jetpack Compose community

---

## 📞 Support

For issues, questions, or suggestions:

- Open an issue on GitHub
- Check existing documentation
- Review troubleshooting section

---

**Built with ❤️ for the UTA Community**
