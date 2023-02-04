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
(and to learn another programming language).

In addition to Scala itself, you will need a number of other components.  As I have finally
brought myself out of the dark ages and dropped the Makefile, Maven should take care of
everything.  (Yes, yes, sbt or gradle... more new things...)

Running the program
-------------------
Run as with any other java executable:
```
java -jar jTrapKATEditor.jar
```
(or double-click on Windows - assuming your JRE is installed correctly).
