# WiFi Status Logger

Copyright 2021 Crossmatch Technologies, Inc. All rights reserved

The WiFi Status Logger is an Android program to log Wi-Fi status state 
changes (connected/disconnected) to a local log file.
The purpose of this program is to log when the Wi-Fi connection drops and when
it reconnects to see if there is a pattern across multiple units when the 
Wi-Fi connection is lost due to interference, time of day, etc.

In order to correlate the logs across multiple units it is recommended to 
sync all handheld devices clocks to a master NTP (Network Time Protocol) server and
enable all the handheld devices to use this server under Settings | Date & time |
Automatic data & time. 
If the devices are on a private network it is possible to setup a local NTP server
and sync them to this server by setting the `ntp_server` config using adb.
The default NTP server is `time.android.com`. To override this and use 
the `pool.ntp.org` server for example set this via adb:

	# adb shell settings put global ntp_server pool.ntp.org
	
replace `pool.ntp.org` with either an internal NTP server name or IP address ot
the machine running an NTP server.

The log is accessible via external USB port at:

	/sdcard/Android/data/com.crossmatch.wifistatuslogger/files/WiFi_Monitor.log

This will log the current Wi-Fi status: connected or connection lost along with
the current time stamp. The log will look like this:

	2021-05-07 10:47:58 [WiFiConnectionMonitor]:Wi-Fi Lost
	2021-05-07 10:48:44 [WiFiConnectionMonitor]:Wi-Fi Available

This program will also display a pop-up message to the user when the Wi-Fi status 
changes. The pop-up can be disabled under the application Settings. 

The log file is rotated when it reaches about 1 MB in size. When this happens, the
current WiFi_Monitor.log is renamed to WiFi_Monitor-1.log and a new WiFi_Monitor.log
file is created. 


## Building the application

Import into Android Studio. There are no special requirements beyond Android 
Studio.

The program does request the following permissions:


* ACCESS_WIFI_STATE
* ACCESS_NETWORK_STATE
* RECEIVE_BOOT_COMPLETED
* WRITE_EXTERNAL_STORAGE
* READ_EXTERNAL_STORAGE

The first 2 are needed to get access to the Wi-Fi/network status. 
The RECEIVE_BOOT_COMPLETED is needed so the program will automatically start.
The last 2 are needed to read/write the log file.

## Running the program:

The program is currently setup to automatically launch at system startup so that it
is always running. This means the application will be displayed to the user
when the system starts up. It's OK to navigate away from the application and it
will continue to run in the background as long as it isn't killed off manually
or by Android itself.

If you don't want to see the pop-up messages when the Wi-Fi status changes, 
launch the application, select Settings (the three bars at the top right)
and turn the pop-ups off.

The log file will show when the Wi-Fi connection goes up or down. When Wi-Fi
comes back up it might show several "Wi-Fi Available" messages depending on
the Android version. Android Lollipop seems to send out the message 3 times when
the Wi-Fi connection is restored. The timestamp can be used to filter these to see 
if there is any pattern to the Wi-Fi drop outs.

The Wi-Fi status will change and will be logged during "normal" operation as 
well. For example if the Wi-Fi is manually turned on/off or the device goes to 
sleep this will also cause a change in the Wi-Fi state that will be logged. 
On Android 9 (Pie), 
the Wi-Fi will also automatically disconnect if it notices the connection has
not been used for a while as would happen if the device is just left connected
to a charger for example and has gone to sleep (screen is off). 

### Deleting Log Files

The log files can be deleted on the device at any time in order to "reset"
the logging. A new log file will be created if/when Wi-Fi connectivity changes again.

## Extracting the Log file(s):

The log can be pulled from the device through MDM software (from Soti, Cisco, etc.),
ADB (Android Debug Bridge), or using the file transfer over a USB cable. 

### Pull log using USB File Transfer

On devices running Android Lollipop (5.1.x), the USB File Transfer mode is enabled by default so
connecting a USB cable to a remote Windows PC should pop-up a selection dialog
to let you see the files on the device. Just follow the path to 

For devices running Android Pie (9), the file transfer mode must be manually 
enabled each time you wish to use it. To do this pull down the menu from the top
screen to you see the notifcation message that says:

	"Charging this device via USB"
	Tap for more options

click on the message and it will pop-up a USB Preferences dialog. Find the 
setting for `Use USB for` and select the `File Transfer` option. This will allow
you to open the shared storage using Windows File Explorer. Then follow 
the path to:

	Internal shared storage\Android\data\com.crossmatch.wifistatuslogger\files
	
Here you will find the log file and it's backup (if present):

 * WiFi_Monitor.log
 * WiFi_Monitor-1.log

You can just drag-n-drop those files to your local machine for inspection.

### Pull log using ADB:

Enable USB debugging on the device, connect a USB cable to Sentry and on 
the remote computer type:

	# adb pull /sdcard/Android/data/com.crossmatch.wifistatuslogger/files/WiFi_Monitor.log
		and/or the previous log file (if present).
	# adb pull /sdcard/Android/data/com.crossmatch.wifistatuslogger/files/WiFi_Monitor-1.log


