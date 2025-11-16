# Lost and Found App

A modern Android mobile application built with Kotlin and Firebase that helps users report and find lost items on campus or in their community.

## 📱 Features

- **User Authentication**

  - Email/Password registration and login
  - Google Sign-In integration
  - Secure user profile management

- **Item Management**

  - Post lost items with photos, descriptions, and location
  - Post found items to help others
  - Browse all active lost/found listings
  - Search and filter items by category, location, or keywords
  - Update or delete your own posts

- **Real-time Messaging**

  - Direct messaging between users
  - Chat about specific items
  - Real-time message notifications
  - Message history and chat management

- **User Profiles**
  - View your posted items
  - Edit profile information
  - Track item status (active, resolved)

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3 Design
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase
  - Firebase Authentication
  - Cloud Firestore (Database)
  - Firebase Storage (Image uploads)
  - Firebase Cloud Messaging (Notifications)
- **Dependency Injection:** Manual injection
- **Image Loading:** Coil
- **Asynchronous Programming:** Kotlin Coroutines & Flow

## 📋 Prerequisites

- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** Java Development Kit 17
- **Android SDK:** Minimum API 24 (Android 7.0), Target API 34 (Android 14)
- **Gradle:** 8.2 (included via wrapper)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/aroudrasthakur/lostandfound.git
cd lostandfound
```

### 2. Set Up Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "LostAndFound"
3. Add an Android app with package name: `com.lostandfound.app`
4. Download `google-services.json`
5. Place it in the `app/` directory

#### Enable Firebase Services:

**Authentication:**

- Navigate to Authentication → Sign-in method
- Enable "Email/Password"
- (Optional) Enable "Google"

**Cloud Firestore:**

- Navigate to Firestore Database → Create database
- Start in production mode
- Choose your region

**Storage:**

- Navigate to Storage → Get started
- Start in production mode

### 3. Configure Firebase Security Rules

**Firestore Rules:**

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

**Storage Rules:**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /items/{itemId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

### 4. Install Java 17

If you don't have JDK 17:

```bash
winget install EclipseAdoptium.Temurin.17.JDK
```

Set JAVA_HOME (optional):

```bash
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
```

### 5. Build the Project

```bash
# Open in Android Studio
# File → Open → Select lostandfound folder
# Wait for Gradle sync to complete

# Or build from command line:
gradlew assembleDebug
```

### 6. Run the App

1. **Using Emulator:**

   - Open Device Manager in Android Studio
   - Create a virtual device (e.g., Pixel 5, API 34)
   - Start the emulator
   - Click Run ▶️ button

2. **Using Physical Device:**
   - Enable Developer Options on your Android device
   - Enable USB Debugging
   - Connect via USB
   - Select device and click Run ▶️

## 📂 Project Structure

```
app/
├── src/main/
│   ├── java/com/lostandfound/app/
│   │   ├── data/
│   │   │   ├── model/              # Data classes (User, Item, Message, Chat)
│   │   │   └── repository/         # Firebase data access layer
│   │   │       ├── AuthRepository.kt
│   │   │       ├── ItemRepository.kt
│   │   │       └── MessageRepository.kt
│   │   ├── ui/
│   │   │   ├── screens/            # UI screens
│   │   │   │   ├── auth/           # Login, SignUp
│   │   │   │   └── home/           # Home feed
│   │   │   ├── theme/              # App theming
│   │   │   ├── navigation/         # Navigation graph
│   │   │   └── MainActivity.kt
│   │   └── LostAndFoundApplication.kt
│   ├── res/                        # Resources (layouts, strings, colors)
│   └── AndroidManifest.xml
├── build.gradle.kts
└── google-services.json            # ⚠️ Required from Firebase Console
```

## 🎨 App Screenshots

_Screenshots coming soon_

## 🔑 Key Components

### Data Models

- **User:** Stores user profile information
- **Item:** Represents lost or found items with details
- **Message:** Individual chat messages
- **Chat:** Conversation threads between users

### Repositories

- **AuthRepository:** Handles user authentication and profile management
- **ItemRepository:** Manages CRUD operations for items
- **MessageRepository:** Real-time messaging functionality

### ViewModels

- **AuthViewModel:** Authentication state management
- **HomeViewModel:** Item feed management

## 📱 Minimum Requirements

- Android 7.0 (API 24) or higher
- Internet connection for Firebase services
- Camera permission for photo uploads (optional)
- Storage permission for selecting images

## 🐛 Common Issues

### Build Fails with "google-services.json not found"

**Solution:** Download from Firebase Console and place in `app/` directory

### Java Version Incompatibility

**Solution:** Ensure JDK 17 is installed and set as JAVA_HOME

### Launcher Icon Not Found

**Solution:** Already fixed - using vector drawables

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is available for educational purposes.

## 👥 Authors

- Group 8 - CSE 3310

## 📞 Support

For support, create an issue in the GitHub repository.

## 🔮 Future Enhancements

- [ ] Push notifications for new messages
- [ ] Map integration for item locations
- [ ] Category filtering
- [ ] Item status updates (found/claimed)
- [ ] Image gallery for multiple photos
- [ ] QR code generation for items
- [ ] Dark mode support
- [ ] Offline mode with local caching

## 📚 Documentation

For detailed setup instructions, see [SETUP_GUIDE.md](SETUP_GUIDE.md)
