EclipseJava7Refresher
================

This is an Eclipse plugin that uses the new WatchService in Java7 to automatically refresh files in your workspace. This is useful if you edit files outside of Eclipse often, and find yourself frequently F5ing your workspace. This is different from my other project, EclipseJnotifyRefresher, in its implementation. The other project uses the open source jnotify library to provide filesystem change hooks, while this project uses the hooks built-in to Java7.


Installation
================
- Exctract [the zip](https://github.com/psxpaul/EclipseJava7Refresher/raw/master/java7_refresh_plugin.zip) into {ECLIPSE_DIRECTORY}/dropins
- Start Eclipse
- Go to Window->Preferences->General->Workspace
- Enable the "Refresh using native hooks or polling" option


Normally, the above workspace option can cause performance issues on Linux and OSX, as there is no native hook implementation in Eclipse. This plugin leverages new APIs in Java7 to hook into filesystem changes without polling. The result, is that you see your file changes in realtime in Eclipse, with no performance hit!
