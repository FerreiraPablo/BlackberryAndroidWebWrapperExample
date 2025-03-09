# BlackBerry Android Sample App

## Overview
This is a sample Android application specifically designed for BlackBerry devices running Android 4.3 (API Level 18).

## Requirements
- Android Studio
- Android SDK 4.3 (API Level 18)

## Important areas

### Automatic Web Source Download

On "app/build.gradle", there is a ```tasks.register('downloadWebSources') ``` task that downloads the web sources from an url. The task is executed before the build process. 

You can also add your sources manually into the "app/src/main/assets" folder.

### Web Sources
You can change the Source URL of the webview to work locally or remotely on the MainActivity.java file.

## Setup
1. Clone this repository
2. Open project in Android Studio
3. Ensure to have the API Level 18 SDK installed

## Testing
Tested on:
- BlackBerry Q20
- Android 4.3 Emulator

## License
This project is licensed under the MIT License - see the LICENSE file for details.
