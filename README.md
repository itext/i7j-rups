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

##### Troubleshooting

-- Error running mvn clean package...

mvn clean package
[ERROR] Error executing Maven.
[ERROR] java.lang.IllegalStateException: Unable to load cache item
[ERROR] Caused by: Unable to load cache item
[ERROR] Caused by: Could not initialize class com.google.inject.internal.cglib.core.$MethodWrapper

This may be caused by using an unsupported version of java. Try downgrading or upgrading your java version as appropriate.

-- Error generating javadocs

[ERROR] MavenReportException: Error while generating Javadoc: Unable to find javadoc command: The environment variable JAVA_HOME is not correctly set.
org.apache.maven.reporting.MavenReportException: Unable to find javadoc command: The environment variable JAVA_HOME is not correctly set.

Make sure to define your JAVA_HOME pointing to your jdk installation folder. Or make sure that javadoc is on your command path.

-- Warnings downlaoding package-lists

[WARNING] Error fetching link: http://dom4j.github.io/apidocs/package-list. Ignored it

Some package lists may be unavailable (404 error). This will not cause the build to fail so can be ignored.

