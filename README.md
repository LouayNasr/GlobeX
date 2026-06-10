# GlobeX

GlobeX is a modern Android application built with Jetpack Compose that provides real-time currency
exchange rates and conversion tools. It leverages the latest Android technologies and follows Clean
Architecture principles to ensure a robust, scalable, and maintainable codebase.

## 🚀 Features

- **Live Currency Rates**: View up-to-date exchange rates for various currencies.
- **Currency Converter**: Easily convert between different world currencies.
- **Personalized List**: Swipe to remove currencies and manage visible rates for a tailored
  experience.
- **Dark Mode Support**: Fully supports dynamic theme switching (Dark/Light mode).
- **Offline Support**: Basic caching and snackbar notifications for connectivity status (Coming
  Soon).

## 🛠 Tech Stack

- **UI**: [Jetpack Compose]
- **Architecture**: MVI + Clean Architecture
- **Dependency Injection**: [Hilt]
- **Networking**: [Retrofit]
- **Navigation**: [Navigation 3]
- **Local Storage**: [DataStore Preferences]
- **Image Loading**: [Coil]
- **Serialization**: [Kotlin Serialization] & [GSON]
- **Asynchronous Work**: [Kotlin Coroutines] & [Flow]

## 🏗 Architecture

The project is structured following Clean Architecture principles, divided into layers:

- **Data Layer**: Handles API requests, data persistence, and repository implementations.
- **Domain Layer**: Contains business logic, use cases, and repository interfaces.
- **Presentation Layer**: Manages UI state and Compose components using ViewModel following the MVI
  pattern.

## 🚦 Getting Started

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer.
- JDK 11 or higher.
- Android SDK 26+.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/louaynasr/GlobeX.git
   ```
2. Open the project in Android Studio.
3. Sync Project with Gradle Files.
4. Run the app on an emulator or a physical device.

## 🧪 Testing

The project includes unit and instrumentation tests:

- **Unit Testing**: JUnit, MockK, Turbine.
- **UI Testing**: Compose UI Test, Hilt Testing.

To run tests:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📝 Roadmap

- [ ] Add Currency Widgets (JetNews style).
- [ ] Implement user onboarding/orientation.
- [ ] Migrate to API V2.
- [ ] Enhance offline capabilities

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
