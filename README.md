Fakelyze AI Image Detection App

Detect AI-generated images in real-time with machine learning

Fakelyze is an Android application that leverages TensorFlow Lite to classify whether images are AI-generated or authentic. Built with modern Android architecture, the app combines on-device ML inference with an intuitive Material 3 UI, user authentication, and a premium subscription system.

Features

Core Functionality
- Real-time Image Classification** — Capture images via camera or select from gallery
- Confidence Scoring** — Get detailed confidence metrics for each classification
- Detection History** — Browse, filter, and manage past detection results
- Detailed Results** — View in-depth analysis with explanations for each detection

User Experience
- Onboarding Flow** — Guided introduction to app features for new users
- User Authentication** — Secure login, registration, and profile management
- Activity Statistics** — Track scan count, AI detections, and real images analyzed
- Responsive Material 3 UI** — Built entirely with Jetpack Compose for modern design

Monetization & Access Control
- Free vs Premium Tiers** — Feature-gated access with distinct plan benefits
- Daily Scan Limits** — Free users limited to 10 scans/day; premium users unlimited
- Premium Features** — Unlimited scans, advanced analytics, result sharing, cloud backup
- Easy Upgrade/Downgrade** — Seamless subscription management

Architecture

Design Pattern
- **MVVM** with unidirectional data flow for clean separation of concerns
- **Kotlin Coroutines & Flows** for reactive, non-blocking state management
- **Dependency Injection** with Koin for modular, testable code

Tech Stack
| Component | Technology |
|-----------|-----------|
| **UI Framework** | Jetpack Compose + Material 3 |
| **Navigation** | Jetpack Navigation Compose |
| **Local Database** | Room ORM with TypeConverters |
| **ML Inference** | TensorFlow Lite (on-device) |
| **Dependency Injection** | Koin |
| **Async Processing** | Kotlin Coroutines & Flow |
| **Data Storage** | DataStore + FileProvider |
| **Language** | Kotlin |
| **Min SDK** | Android 8.0 (API 26) |

Project Structure
