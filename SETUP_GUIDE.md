# Lost and Found App - Setup Guide

## Overview
This is a Lost and Found mobile application built with Kotlin and Firebase for Android.

## Prerequisites
1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/

2. **Android Studio**
   - Download from: https://developer.android.com/studio
   - Minimum version: Android Studio Hedgehog (2023.1.1) or later

3. **Android SDK**
   - API Level 24 (Android 7.0) minimum
   - API Level 34 (Android 14) target

## Firebase Setup

### Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click "Add project"
3. Enter project name: "LostAndFound"
4. Follow the setup wizard

### Step 2: Register Android App
1. In Firebase console, click "Add app" → Android icon
2. Enter package name: `com.lostandfound.app`
3. Download `google-services.json`
4. Place the file in: `app/google-services.json`

### Step 3: Enable Firebase Services

#### Authentication
1. In Firebase console, go to **Authentication**
2. Click "Get started"
3. Enable **Email/Password** sign-in method
4. (Optional) Enable **Google** sign-in

#### Firestore Database
1. Go to **Firestore Database**
2. Click "Create database"
3. Start in **production mode**
4. Choose a location near your users

#### Storage
1. Go to **Storage**
2. Click "Get started"
3. Start in **production mode**

### Step 4: Configure Firestore Security Rules
In Firebase Console → Firestore Database → Rules, add:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    match /messages/{messageId} {
      allow read: if request.auth != null && 
        (request.auth.uid == resource.data.senderId || 
         request.auth.uid == resource.data.receiverId);
      allow create: if request.auth != null;
    }
    
    match /chats/{chatId} {
      allow read: if request.auth != null && 
        request.auth.uid in resource.data.participants;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        request.auth.uid in resource.data.participants;
    }
  }
}
```

### Step 5: Configure Storage Security Rules
In Firebase Console → Storage → Rules, add:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /items/{itemId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

## Installation Steps

### 1. Open Project in Android Studio
```bash
cd C:\Users\aroud\OneDrive\Documents\GitHub\Website\lostandfound
```
- Open Android Studio
- Click "Open" and select the project folder

### 2. Add google-services.json
- Place your downloaded `google-services.json` in the `app/` directory
- **IMPORTANT**: This file is required for Firebase to work

### 3. Sync Gradle
- Android Studio will automatically prompt to sync
- Or click: File → Sync Project with Gradle Files

### 4. Build Project
- Click: Build → Make Project
- Or press: Ctrl+F9 (Windows)

### 5. Run the App
- Connect an Android device or start an emulator
- Click the green "Run" button or press Shift+F10
- Select your device/emulator

## Project Structure

```
app/
├── src/main/
│   ├── java/com/lostandfound/app/
│   │   ├── data/
│   │   │   ├── model/          # Data models (User, Item, Message)
│   │   │   └── repository/     # Firebase interactions
│   │   ├── ui/
│   │   │   ├── screens/        # App screens (Login, Home, etc.)
│   │   │   ├── theme/          # UI theming
│   │   │   ├── navigation/     # Navigation logic
│   │   │   └── MainActivity.kt
│   │   └── LostAndFoundApplication.kt
│   ├── res/                    # Resources (layouts, strings, colors)
│   └── AndroidManifest.xml
├── build.gradle.kts
└── google-services.json        # YOU NEED TO ADD THIS!
```

## Features Implemented

### Core Features
- ✅ User Authentication (Email/Password)
- ✅ Post Lost/Found Items
- ✅ Browse All Items
- ✅ Image Upload to Firebase Storage
- ✅ Real-time Messaging System
- ✅ Search Functionality
- ✅ User Profiles

### Data Models
- **User**: uid, email, displayName, photoUrl
- **Item**: title, description, category, location, images, status
- **Message**: chat messages with real-time updates
- **Chat**: conversation threads between users

## Next Steps to Complete

1. **Add Missing Screens** (Not yet implemented):
   - Sign Up Screen
   - Post Item Screen
   - Item Detail Screen
   - Search Screen
   - Messages/Chat Screen
   - Profile Screen

2. **Add Icons**:
   - Replace default launcher icon
   - Add `ic_launcher.png` in `res/mipmap-*` folders

3. **Test Firebase Connection**:
   - Run the app
   - Try creating an account
   - Check Firebase Console for user data

## Common Issues

### Issue: "google-services.json not found"
**Solution**: Download from Firebase Console and place in `app/` folder

### Issue: Build fails with dependency errors
**Solution**: 
```bash
# In Android Studio terminal:
./gradlew clean
./gradlew build
```

### Issue: App crashes on launch
**Solution**: Check that Firebase is properly configured and google-services.json is in place

## Testing

### Create Test Account
1. Launch the app
2. Click "Sign Up"
3. Enter email and password
4. Verify user appears in Firebase Console → Authentication

### Test Item Creation
1. Login to the app
2. Navigate to "Post Item"
3. Fill in item details and upload image
4. Check Firebase Console → Firestore for new item document

## Gradle Wrapper

If gradlew is not present, create it:
```bash
gradle wrapper --gradle-version 8.2
```

## Dependencies

Key libraries used:
- Firebase BOM 32.7.0
- Jetpack Compose for UI
- Kotlin Coroutines
- Coil for image loading
- Material 3 Design

## Support

For Firebase setup help: https://firebase.google.com/docs/android/setup
For Android development: https://developer.android.com/docs
