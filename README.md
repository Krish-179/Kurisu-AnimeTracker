# Kurisu-AnimeTracker
Kurisu is an Android app that helps you manage, track, and update your anime progress *automatically*. It integrates with MyAnimeList (MAL) and AniList APIs, automatically updating your list when an episode is 90% watched.
This is my first big project built with *Jetpack Compose*.

⚠ *Note:* This app currently supports *offline downloaded anime* only and *MP4* files. Other streaming sources or file formats (like MKV) are *not supported*.

---

## ✨ Features
- 📂 *Library Screen*  
  - Add folders and organize anime files.  
  - View list of added folders with metadata (last used, number of episodes).  
  - Quick navigation via bottom bar.  

- 📊 *Tracking Screen*  
  - *Primary Tabs*:  
    - *Watching* → Shows progress of local anime.
    - *Tracking* → Contains *secondary tabs*:  
      - *MAL* → Displays user list from MyAnimeList API.  
      - *AniList* → Displays user list from AniList API.  
      - *Others* → Placeholder tab (currently empty).  

- ⚙ *Settings Screen*  
  - *Tracker*:  
    - Toggle auto-update for lists.  
    - Connect with MAL and AniList accounts.  
  - *Preferences*:  
    - Enable/disable notifications when a list is updated.  
  - *About*:  
    - (Currently placeholder, not linked to navigation).  
