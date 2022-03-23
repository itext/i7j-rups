RUPS - Research Edition
====
_"The 'R' in RUPS stands for Research"_ - M. Gandhi

iText RUPS is a tool to view PDF structure in a Swing GUI.

### Research Features!

Of course, no experimental build is complete without any experimental features! Do note, these are experimental, not to be included in the public builds! And as such, they can be rough around the edges. We are always open to pull requests for any kind of feature you want implemented. Or if you want to pitch an idea to us for RUPS; you can find us on the #research channel on slack!

Without further ado, an overview of the features available in this edition:

* Content Stream Syntax Checker
* XFDF Merging


### Building and running

Before you build this **super** _special_ RUPS Research Edition, there's a few things you need to build and have locally on your machine.
Luckily the Research Team has you covered:

Follow the build instructions in the following projects (or if absent, just run `mvn clean install`)
- pdfCop ( https://git.itextsupport.com/projects/RESEARCH/repos/pdfcop/browse )
- XFDF-Merger ( https://git.itextsupport.com/users/matthias.valvekens/repos/xfdf-merger/browse )


These should allow you to continue with the regular build instructions as outlined below:

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