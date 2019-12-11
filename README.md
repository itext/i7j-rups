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

If you are a Windows user, there is a possibility to create a RUPS executable (`exe` file). It still requires Java to run, but you will not need to type the command anymore each time.

To build an executable, run the following command:

```
mvn clean package -P exe
```

This should produce `Rups.exe` in your `target` folder which you can run as any other executable assuming you have Java installed on your system.