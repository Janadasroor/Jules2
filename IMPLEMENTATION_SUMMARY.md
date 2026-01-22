# Jules Android App Implementation

## Overview
This Android application manages Jules sessions, allowing users to:
1.  **List Sessions**: View all active and past sessions.
2.  **Create Sessions**: Start a new session with a prompt and a selected source context.
3.  **View Session Details**: See session metadata, current state, prompt, and outputs.
4.  **Interact with Sessions**:
    *   **Chat**: Send messages to the agent and view the conversation history (activities).
    *   **Approve Plans**: Approve generated plans when the session is in `AWAITING_PLAN_APPROVAL` state.
5.  **View Activities**: See a chronological log of what happened in the session, including agent messages, user messages, plan generation, and errors.

## Architecture
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose
*   **Navigation**: Navigation Compose
*   **Networking**: Retrofit + OkHttp
*   **State Management**: ViewModel + StateFlow

## Key Components
*   **`MainActivity`**: Sets up the navigation host.
*   **`SessionListScreen`**:
    *   Fetches and lists sessions.
    *   Provides a Floating Action Button to create new sessions.
    *   Dialog to enter prompt and select a Source from the API.
*   **`SessionDetailScreen`**:
    *   Displays full details of a session.
    *   Lists activities (chat/event history).
    *   Provides input to send messages.
    *   Shows "Approve Plan" button when applicable.
*   **`SessionViewModel`**:
    *   Centralized logic for API calls (`fetchSessions`, `getSession`, `createSession`, `sendMessage`, `approvePlan`, `fetchSources`).
    *   Manages state for `sessions`, `selectedSession`, `activities`, `sources`, `loading`, and `error`.
*   **`JulesApiService`**:
    *   Retrofit interface defining endpoints: `listSessions`, `createSession`, `getSession`, `sendMessage`, `listSources`, `approvePlan`, `listActivities`.
*   **Models**: Data classes mirroring the Jules API resources (`Session`, `Source`, `Activity`, `Plan`, etc.).

## Setup
The project is configured with proper dependencies in `libs.versions.toml` and `build.gradle.kts`.
Build with: `./gradlew assembleDebug`
