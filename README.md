# GlobeX

GlobeX is a modern Android application built with Jetpack Compose that provides real-time currency
exchange rates and conversion tools. It leverages the latest Android technologies and follows Clean
Architecture principles to ensure a robust, scalable, and maintainable codebase.

## 🚀 Features

- **Live Currency Rates**: View up-to-date exchange rates for various currencies.
- **Currency Converter**: Easily convert between different world currencies.
- **Dark Mode Support**: Fully supports dynamic theme switching (Dark/Light mode).
- **Offline Support**: Basic caching and snackbar notifications for connectivity status (Coming
  Soon).

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture**: MVI + Clean Architecture
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Networking
  **: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Navigation**: [Navigation 3](https://developer.android.com/jetpack/compose/navigation) (
  Experimental)
- **Local Storage
  **: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Serialization
  **: [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html) & [GSON](https://github.com/google/gson)
- **Asynchronous Work
  **: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)

## 🏗 Architecture

The project is structured following Clean Architecture principles, divided into layers:

- **Data Layer**: Handles API requests, data persistence, and repository implementations.
- **Domain Layer**: Contains business logic, use cases, and repository interfaces.
- **Presentation Layer**: Manages UI state and Compose components using ViewModel.

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
