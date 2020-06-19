<img src="hero.png"/>

# +1 Dark Mode
### Get one of the best Android 10 features with some custom automatic trick.

OnePlus devices are missing a fundamental Android 10+ feature: the dark mode.  
Get a quick settings tile and enable a few automatic tricks to get the best from the Dark Theme.

Help me developing with feedback and support me on how you can.
<div style="text-align:center"><a href="https://play.google.com/store/apps/details?id=com.tommasoberlose.darkmode" target="_blank"><img src="google-play-badge.png" height="100" /></a></div>

<br />
<br />

WRITE_SECURE_SETTINGS Permission
-------
1. #### Enable Developer Options.
Go to the system settings, open the device info screen (where there is the Android version), then tap 7 times on Build Number.

2. #### Enable USB Debugging
Open the Developer Options setting and enable USB Debugging.  
Connect your phone to the computer via USB cable. If a popup is displayed on your device, click Allow.

3. #### Download ADB
Download ADB (Android Drive Bridge) from <a href="https://developer.android.com/studio/releases/platform-tools.html" target="_blank">here</a> and install it on your computer.

4. #### Grant the permission
Open a terminal and move to the folder where you have installed the ADB.  
Then run the follow command:  <br /><br />
`./adb shell pm grant com.tommasoberlose.darkmode android.permission.WRITE_SECURE_SETTINGS`<br /><br />
Based on your OS you'll need to execute the command `./adb [...]` or `.\adb [...]`.

5. #### Restart the app
If everything worked you can restart the app and start using the Dark Mode on your device.


License
-------
Copyright (C) 2017-2020 Tommaso Berlose (http://tommasoberlose.com)

Another Widget binaries and source code can be used according to the [MIT Licence](LICENSE).
