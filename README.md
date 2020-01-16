RUPS
====

iText RUPS is a tool to view PDF structure in a Swing GUI.

### Building and running

iText Rups is built into a Jar file which is then run by Java. To build iText Rups with Maven, run the following command:
```
mvn clean package
```

The resultant Jar file with dependencies will be located in the `target` directory.

To run RUPS, simply type

```
java -jar itext-rups-x.y.z-SNAPSHOT.jar
```

##### Creating an executable file on Windows

If you are a Windows user, there is a possibility to create a RUPS executable (`exe` file). It still requires Java Runtime Environment to run, but you will not need to type the command anymore each time.

To build an executable, run the following command:

```
mvn clean package -P exe
```

This should produce `Rups.exe` in your `target` folder which you can run as any other executable assuming you have Java installed on your system.

##### Creating an application bundle (.app) on macOS

Similar to Windows executable, there is a possibility to create a RUPS application bundle (`.app` application) for macOS. Again, it requires Java Runtime Environment to run. Mind that in order to build it one requires Java version prior to 10, additionally proper application bundle can only be built on macOS.

To create application bundle, run the following command:

```
mvn clean package -P mac
```

This will produce `itext-rups.app` in your `target` folder (in itext-rups-{project.version} subfolder). You can copy it to your 'Applications' folder and run it as any other application assuming you have Java installed on your system. Maven 'appbundle' should handle executable file permissions automatically, but you can always give the rights manually like this: `chmod +x /Applications/itext-rups.app/Contents/MacOS/*`.