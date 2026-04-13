# Android Media Player App

A modern, high-performance **Android Media Player** built using **Media3 ExoPlayer**, designed with a clean UI and efficient architecture.  
This project focuses on real-world media handling, performance optimization, and scalable design.

---

## Features

### Playback System
- Media3 ExoPlayer integration
- Supports both **audio & video playback**
- Smooth seek & resume functionality
- Background playback using Service
- URI-based playback (no position-based bugs)

---

### Player Interface
- Clean and minimal video player UI
- Integrated Media3 PlayerView
- Custom UI adjustments (removed default unwanted elements)
- SeekBar for precise playback control
- Smooth play/pause and resume handling
- Optimized for dark mode experience

---

### Touch Controls
- Gesture-based brightness & volume control
- Gesture-based seek control
- Double-tap to play/pause & seek (forward/backward)
- Long press hold for speed up & lock UI

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

### Advanced Search System
- Room Database + LiveData integration
- Fast in-memory filtering
- Accurate mapping using URI (no mismatch issues)

---

### Performance Optimizations
- Background processing using threads
- Thumbnail caching to avoid redundant work
- Optimized handling of large media files
- Smooth scrolling and UI responsiveness

---

### Additional Features
- Resume last played media
- "New" indicator for recently added media
- Foreground notification toggle (dynamic)
- BottomNavigation-based UI structure

---

### Picture-in-Picture (PiP)
- Supports PiP mode for video playback
- Handles lifecycle transitions properly
- Works with background playback

---

## Architecture

- **PlayerService** → Holds ExoPlayer instance  
- **PlayerActivity** → Handles UI & playback logic  
- **RecyclerView Adapter** → UI only (no direct player access)  

### Key Design Decisions
- URI-based media identity instead of position
- Clear separation of concerns
- Scalable and maintainable structure

---

## Tech Stack

- **Language:** Java  
- **Player:** Media3 ExoPlayer  
- **Database:** Room  
- **Image Loading:** Glide  
- **UI:** RecyclerView, ConstraintLayout  
- **Architecture:** Service-based playback  

---

## Project Highlights

- Handles real-world edge cases (missing thumbnails, large files)
- Avoids common playback bugs (position mismatch, UI lag)
- Efficient caching and threading implementation
- Designed similar to production-level media apps

---
