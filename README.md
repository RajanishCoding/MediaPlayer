# Android Media Player App 🎬

A modern, advanced **Android Media Player** built using **Media3 ExoPlayer** in **Android Studio**, designed with a lightweight UI and real-world media handling capabilities.  
It supports audio/video playback, gesture-based controls, background playback, and optimized media browsing with thumbnail caching.

---

## ⚙️ Features

### Playback System
- Media3 ExoPlayer integration
- Supports both **audio & video playback**
- Smooth seek & resume functionality
- Background playback with notification using Service
- URI-based playback (avoids position mismatch bugs)

---

### Player Interface
- Clean and minimal video player UI
- Integrated Media3 PlayerView
- SeekBar for precise playback control
- Smooth play/pause and resume handling

---

### Touch Controls
- Gesture-based brightness & volume control
- Horizontal swipe for precise seeking
- Double-tap gestures for play/pause and forward/backward seek
- Long press hold for speed up & UI gesture lock

---

### Audio & Subtitle Handling
- Multiple audio track support
- Dynamic track switching
- Subtitle stream selection
- Proper labeling of tracks

---

### Media Listing & Thumbnails
- RecyclerView-based media listing
- MX Player–inspired layout (thumbnail + details)
- Glide integration for efficient thumbnail loading & caching
- Fallback handling for missing thumbnails

---

### Picture-in-Picture (PiP)
- Supports PiP mode for video playback
- Handles lifecycle transitions properly
- Works with background playback

---

### Advanced Search System
- Room Database + LiveData integration
- Optimized in-memory filtering
- Accurate mapping using URI (no mismatch issues)

---

### Performance Optimizations
- Background processing using threading (ExecutorService)
- Thumbnail caching to avoid redundant work
- Optimized handling of large media files
- Smooth scrolling and UI responsiveness

---

### Additional Features
- Resume last played media
- "New" indicator for recently added media
- Foreground notification toggle (dynamic)

---

## 🧩 Architecture

- **PlayerService:** Manages background playback  
- **PlayerActivity:** Handles UI & controls playback logic  
- **RecyclerView Adapter:** Handles listing of media items  

---

## 🧠 Problems Solved
- Prevented playback mismatch using URI-based mapping
- Reduced UI lag using background threading
- Managed ExoPlayer lifecycle efficiently across Activity & Service

---

## 🛠️ Tech Stack

- **IDE:** Android Studio
- **Language:** Java  
- **Player:** Media3 ExoPlayer  
- **Database:** Room
- **Local Storage:** SharedPreferences (for settings & user preferences)
- **Thumbnail Loading:** Glide  
- **UI:** XML (RecyclerView, ConstraintLayout, etc)    

---

## 📄 License

This project is intended for personal and educational viewing only.
You are not allowed to copy, modify, or redistribute this code without permission.
