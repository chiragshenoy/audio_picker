# audio_picker

A Flutter plugin for Android and iOS to pick local Audios. This supports choosing Audio files from the Music App in iOS, and from the File Explorer in Android.

## Introduction
This plugin helps you open a picker to get the absolute path of Audio files from your mobile.
If you ever wanted to play songs in your app, or upload audio files to your server, this plugin helps you get the **absolute path** of the audio that you select.

## Android permission -
Add the following line in your AndroidManifest.xml 

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>


## iOS
Add the following line in your Info.plist 

    <key>NSAppleMusicUsageDescription</key>
    <string>Explain why your app uses music</string>

In the case of iOS, it exports the audio file that you choose from the Music App.

## Usage
    var path = await AudioPicker.pickAudio();

That's it ! You now have the absolute path of the audio file you selected.

Note : In the case of iOS, since we are retrieving the file from the Music App, the export operation may take a few seconds to complete. Hence it's advisable to show a loading indicator to the user while the `await` call executes.

## Upcoming features - 
* Retrieve metadata of the audio file (Name, Duration, etc)
* Multiselect audio files
