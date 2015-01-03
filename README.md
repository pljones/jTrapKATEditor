jTrapKATEditor - an AlternateMode TrapKAT SysEx Editor
======================================================

This project delivers a more friendly way of editing the rich feature settings
of your AlternateMode TrapKAT, versions 3 and 4 (and possibly 5, too).

Because it runs under the Java Virtual Machine, it should run on most computers
(and possibly other devices that have full Java support).

For the latest download, please see [the project website](http://pljones.github.io/jTrapKATEditor/ "the project website").

For support, please see
[this thread](http://www.alternatemode.com/forum/index.php?topic=4140 "jTrapKATEditor for Windows, MacOS and Linux")
on the AlternateMode site (registration required).  Or build from source (see below).


Dependencies
------------
I don't like Java but I wanted to use the JVM.  I discovered that Scala is very much like C#,
in which the earlier version of this program was written and so adopted that for development
(and to learn another programming language).  I have been using Scala 2.11.x for recent builds.

In addition to Scala itself, you will need a number of other components.

I like MigLayout.  You need it.  I'm using [4.0](http://www.miglayout.com/ "MigLayout Downloads"),
rather than the maven version.

I had to resort to some hackery to make the Windows experience comfortable.
This necessitated use of [JNA](https://github.com/twall/jna#download "Download").
You need both JAR files.

I also wanted to make running the program simple.  Once built, all you need to say is:

+ java -jar jTrapKATEditor.jar

or double-click on Windows.
(You can set the file as executable on linux and double-click should then also work.)

For this simplicity, the build depends on P. Simon Tuffs'
[One-Jar](http://one-jar.sourceforge.net/index.php?page=introduction&file=intro "One-Jar Introduction")
packaging system.  You will need to get
[one-jar-boot-0.97.jar](http://one-jar.sourceforge.net/index.php?page=downloads&file=downloads "Downloads")
and unzip it into a directory called "one-jar".  You will then need to ensure the following are in the "lib" directory
(symlinks are good enough) so that they get packaged:

+ jna-4.1.0.jar
+ jna-platform-4.1.0.jar
+ miglayout-4.0-swing.jar
+ scala-library.jar
+ scala-swing.jar
+ scala-xml.jar

I had to do things my own way, naturally, when it comes to the build processes.  Yes, it's scala... so I used a classic Makefile...
