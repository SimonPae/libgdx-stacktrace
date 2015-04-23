LibGDX Remote Stacktrace for Desktop and Android (iOS to be tested, WebGL not working)
===================================
inspired by ["Android Remote Stacktrace: Improved"](https://github.com/Pretz/improved-android-remote-stacktrace) of [Alex Pretzlav](https://github.com/Pretz)



## Client side usage

Download the latest `stacktrace.jar` file [found here](https://raw.githubusercontent.com/SimonPae/libgdx-stacktrace/master/libs/stacktrace.jar)
or paste the GdxStackTraceSender.java to your project or paste the GdxStackTraceSender.java to a new project and add it as a dependency.

You must enable internet access for your android application:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

![internet-permission](http://paeusch.com/libgdx-stacktrace/internet-permission.jpg)

In the create method of your core app (e.g. MyGdxGame.java), you must call either `public GdxStackTraceSender(String postUrl)` (for the default HTTP POST behavior and the default stacktrace file handle) or `public public GdxStackTraceSender(String postUrl, String path, String filename) to modify default stacktrace file path and filename.
To store the stacktrace file on the device (to be able to send the stacktrace later - if no internet connection is available) the [local Gdx filehandle is used](https://github.com/libgdx/libgdx/wiki/File-handling#file-storage-types).

```java
new GdxStackTraceSender("http://your.domain/your-server-file.php");
```

## Server side installation

Since you would like to store your stack traces on your own server or email to your own mail account, you will have to add a smale php file (here: `your-server-file.php`) on your server (here: `http://your.domain/`):

At `http://your.domain/your-server-file.php` the client side implementation will expect to find [this simple PHP script](https://github.com/SimonPae/libgdx-stacktrace/blob/master/server/your-server-file.php), which will take three POST parameters: 'package_name', 'package_version' and 'stacktrace'. The collected data is simply stored in a plain text file. You can extend the script to send you an email with the stack trace if you like - just uncomment the last line and change the email addresses.

## Gradle Integration

Create a `libs` folder in your core project as well as in your android project folder.
Add the stacktrace.jar file to both libs folders.
![libs](http://paeusch.com/libgdx-stacktrace/libs.jpg)
Add to the build.gradle file of the root of the project
in the project(":core") section under dependencies

    compile fileTree(dir: 'libs', include: '*.jar')
    
as well as in the project(":android") section under dependencies

    compile fileTree(dir: 'libs', include: '*.jar')
    
![gradle-integration](http://paeusch.com/libgdx-stacktrace/gradle-integration.jpg)

THIS IS NOT THE BEST SOLUTION, SINCE WE NEED TO COPY THE stacktraces.jar FILE TO TWO LIBS FOLDERS. IF YOU HAVE A SUGGESTION HOW TO ACHIEVE IT IN A SMARTER WAY, PLEASE TELL ME - I WILL ADD YOUR SOLUTION!

## Refresh Dependencies

Refresh dependencies of your projects (android, core) by selecting the two projects -> right click in eclipse -> Gradle -> Refresh all

![eclipse-refresh](http://paeusch.com/libgdx-stacktrace/eclipse-refresh.jpg)

or by using command line -> go to the root of your project -> 

	gradlew -refresh-dependencies
	
![commandline](http://paeusch.com/libgdx-stacktrace/commandline.jpg)
    
## Support

If you have problems, feel free to drop me a mail at dev@paeusch.com

## Troubleshooting

If you can not send stacktraces to your mail account, your mail server might not be set up properly.
Uncomment the filehandle section of the your-trace-file.php to be able to see, if stacktrace files are at least sent to your server.
If you can not send stacktraces from android, you might be missing INTERNET permission.

## Contributors

Thanks to these people, who contributed [to the original peace of code](https://github.com/Pretz/improved-android-remote-stacktrace) with code changes and/or bug reports.

[Alex Pretzlav](https://github.com/Pretz), [Glen Humphrey](http://glendonhumphrey.com/), [Evan Charlton](http://evancharlton.com/), [Peter Hewitt](http://dweebos.com/)

## License


The MIT License

Copyright (c) 2015 Simon Päusch

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
