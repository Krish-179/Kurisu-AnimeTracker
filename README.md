# AnimeTracker App

AnimeTracker is an offline-first anime library manager that helps users organize and watch their locally downloaded anime. It is not lightweight (approx. 200MB), but focuses on reliable playback and smooth progress syncing.

## 🚀 Features

* Users manually add an anime folder
* Requires internet **only once** to link with MAL/Anilist (optional)
* Offline watching supported anytime
* When back online, watch progress auto-syncs with linked services
* Supports **MKV** and **MP4** files
* Uses a **customized VLC player** for stable playback
* Simple UI/UX (still improving)
* About page not implemented yet; several features are work-in-progress

## 🛠️ Tech Stack

* **Android** (Jetpack Compose + Kotlin)
* Room / DataStore (if used for saving progress)
* Media Store APIs for reading files

## ⚙️ How It Works

1. AnimeTracker reads the user's local files.
2. It groups episodes based on folder structure.
3. It shows watch progress using a progress bar.
4. User taps an episode → it opens in their preferred video player.

## 🔒 Privacy

* The app does **not** collect any data.
* No analytics, no online sync.
* Everything stays on the user's device.

## 🐛 Known Issues

* Large folders may take time on first scan.
* Certain file formats may not be recognized depending on device.

## 📈 Roadmap

* Cloud sync (optional)
* Better metadata extraction
* Custom anime cover images
* Episode sorting improvements

---

Thank you for using **AnimeTracker**! 🎬✨
