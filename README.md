# 🌐 Local Web Server & App Launcher (Android, Kotlin)

## 📖 Overview

This project is a **self-contained local server + web app runner** for Android.
It combines an embedded **Ktor file server** with a **custom WebView client**, allowing you to:

* Host and serve web apps (HTML/JS/CSS) locally.
* Download and extract new web packages (`.zip`).
* Manage storage and paths with an in-app file explorer.
* Provide offline fallback when the network/server isn’t available.

The app is built modularly with **Activities for permissions, server control, downloading, file management, and maintenance**.

---

## 📥 Download

👉 [**Download APK here**](https://github.com/kikas45/local_Server/blob/SmarxCellComplete/localServer.apk?raw=true)

---

## 🏗️ Tech Stack

* **Language:** Kotlin
* **Server:** [Ktor](https://ktor.io/) (CIO engine, static file serving, streaming, CORS enabled)
* **UI:** Android SDK, ViewBinding, RecyclerView, AlertDialogs, Custom XML layouts
* **Persistence:** SharedPreferences, Room (via `IndexViewModel`)
* **Networking:** ConnectivityManager, Coroutines, DownloadManager
* **Services:** Foreground `ServerService` to keep the server running

---

## ⚡ Core Features

### 🔌 Server

* Embedded **Ktor server** (`FileServer`) serves files from internal storage.
* Supports **static file hosting** (defaulting to `index.html`).
* Handles **video/audio streaming** via `PartialContent`.
* Configurable **local port and base directory**.

### 📱 Web Client (`DemoWebActivity`)

* Embedded **WebView** with:

  * JavaScript, cookies, geolocation, and multiple windows support.
  * Online → loads from configured server.
  * Offline → falls back to bundled `assets/` HTML.
* Detects network changes and shows user-friendly dialogs.

### ⬇️ Package Management

* **SelectDownloadUrlActivity** → enter & validate server URL.
* **DownlodZipActivity** → download `.zip` with pause/resume/cancel and auto-extract.
* **ExtractZipActivity** → manually pick and extract `.zip` using SAF.
* **ListSavedPathActivity** → quick-launch saved app paths into WebView.

### 📂 File Explorer

* Browse extracted files/folders.
* Delete or copy file paths.
* Saves **last visited folder** for quick access.
* Immersive fullscreen mode with optional branding background.

### 🛠️ Settings & Maintenance

* **SettingMaintenaceActivity** → system shortcuts (WiFi, device settings, exit app).
* Toggle immersive UI / hide status bar.
* Manage server lifecycle (start/stop).

### 🎬 App Flow & Permissions

* **TvActivityOrAppMode** → onboarding wizard that requests all required permissions:

  * Storage, Camera, Audio, Location, Notifications, Overlay, Write Settings, Ignore Battery Optimizations.
* **LightActivity** → splash/init screen that auto-starts server if enabled.
* **SeverEngineActivity** → the **control center** to start/stop the server, configure ports, and launch client.

---

## 🚀 Typical Workflow

1. On first launch → **TvActivityOrAppMode** ensures permissions are granted.
2. App initializes via **LightActivity** (auto-start server if enabled).
3. User enters a package URL in **SelectDownloadUrlActivity** → downloads & extracts via **DownlodZipActivity**.
4. Extracted files are hosted by **FileServer** and launched in **DemoWebActivity**.
5. Saved app paths can be quickly reopened via **ListSavedPathActivity**.
6. Files can be managed through **FileExplorerActivity**.
7. Maintenance & system shortcuts are available in **SettingMaintenaceActivity**.

---

## 📦 Project Modules

* `authServerMood/FileServer.kt` → Embedded Ktor server.
* `DemoWebActivity` → Main WebView client.
* `DownlodZipActivity`, `ExtractZipActivity`, `SelectDownloadUrlActivity` → Download & extraction flows.
* `ListSavedPathActivity` → Manage/launch saved paths.
* `FileExplorerActivity` → In-app file manager.
* `LightActivity` → Splash/server auto-start.
* `SeverEngineActivity` → Server control panel.
* `SettingMaintenaceActivity` → Maintenance/system tools.
* `TvActivityOrAppMode` → First-run permission wizard.

---

## 📸 Screenshots

**Permissions Setup**
![Permissions Screen](https://github.com/kikas45/local_Server/blob/SmarxCellComplete/WhatsApp%20Image%202025-09-10%20at%203.40.46%20PM.jpeg?raw=true)

**Server Control Panel**
![Server Screen](https://github.com/kikas45/local_Server/blob/SmarxCellComplete/WhatsApp%20Image%202025-09-10%20at%203.41.13%20PM.jpeg?raw=true)

---

## 🔮 Future Improvements

* Add **encryption/authentication** for hosted content.
* Improve **UI/UX** with Material 3 & animations.
* Add **logs/analytics dashboard** inside the app.
* Provide **auto-update of hosted packages**.

---

👨‍💻 Built with **Kotlin + Android SDK + Ktor**

---

