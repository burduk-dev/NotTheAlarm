# NotTheAlarm

An Android alarm clock that selects wake-up audio by tags from your own music library and internet radio.

## Stack
- Kotlin
- Jetpack Compose
- Android AlarmManager
- Media3 ExoPlayer

## Open the project
Open this repository in Android Studio (Giraffe or newer), allow Gradle sync, then run the `app` configuration on an Android device.

## MVP status
This repository is being bootstrapped. The first version includes alarm scheduling, tag-based source selection, local audio import, radio station management, and a simple Compose interface. Exact alarms require the appropriate Android system access; the app should guide the user to grant it.

## Notes
- Local tracks are imported through Android's system file picker; the app does not need broad storage access.
- Radio playback requires internet connectivity. A local track is used as fallback when a matching radio station cannot be played.
- Radio URLs must point to direct audio streams (for example, `.mp3`, `.aac`, or a compatible HLS stream), not station homepages.
