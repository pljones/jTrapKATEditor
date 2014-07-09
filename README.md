jTrapKATEditor - a JVM TrapKAT SysEx Editor
===========================================

With the slow demise of MONO precluding my using .Net on anything except
MSWindows, I have given in and decided to port my TrapKAT SysEx Editor
to the JVM.

The target is to have it feature (and probably bug) identical.


Dependencies
------------

I like MigLayout.  You need it.  I'm using [4.0](http://www.miglayout.com/ "MigLayout Downloads"),
rather than the maven version.

I had to resort to some hackery to make the Windows experience comfortable.
This necessitated use of [JNA](https://github.com/twall/jna#download "Download").
You need both JAR files.

I wanted to make running the program simple.  Once built, all you need to say is:

+ java -jar jTrapKATEditor.jar

or double-click on Windows.  To make this work, the build
depends on P. Simon Tuffs' [One-Jar](http://one-jar.sourceforge.net/index.php?page=introduction&file=intro "One-Jar Introduction")
packaging system.  If you want to build from source, get
[one-jar-boot-0.97.jar](http://one-jar.sourceforge.net/index.php?page=downloads&file=downloads "Downloads") and unzip
it into "one-jar".

I had to do things my own way, naturally, when it comes to the build processes.  Yes, it's scala... so I used a classic Makefile...
