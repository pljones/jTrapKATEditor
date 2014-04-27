jTrapKATEditor - a JVM TrapKAT SysEx Editor
===========================================

With the slow demise of MONO precluding my using .Net on anything except
MSWindows, I have given in and decided to port my TrapKAT SysEx Editor
to the JVM.

The target is to have it feature (and probably bug) identical.


Dependencies
------------

Running the jar file should be as simple as

+ java -jar jTrapKATEditor.jar

or double-clicking on Windows.  To make this work, the build
depends on P. Simon Tuffs' [One-Jar](http://one-jar.sourceforge.net/index.php?page=introduction&file=intro "One-Jar Introduction")
packaging system.  If you want to build from source, get
[one-jar-boot-0.97.jar](http://one-jar.sourceforge.net/index.php?page=downloads&file=downloads "Downloads") and unzip
it into "one-jar", then use the Makefile.  (I had to do things
my own way, naturally.)
