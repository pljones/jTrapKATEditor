all: jTrapKATEditor.jar

jTrapKATEditor.jar: main.jar one-jar/boot-manifest.mf one-jar/lib
	rm -f jTrapKATEditor.jar
	jar cmf one-jar/boot-manifest.mf jTrapKATEditor.jar -C one-jar/ .

main.jar: compile
	rm -rf one-jar/main
	mkdir one-jar/main
	jar cmf META-INF/MANIFEST.MF one-jar/main/main.jar -C bin/ .

compile:
	rm -rf bin/info
	(cd src; scalac -classpath ../lib/miglayout-4.0-swing.jar -d ../bin -feature -optimise info/drealm/scala/*.scala info/drealm/scala/*/*.scala)

one-jar/boot-manifest.mf: one-jar/META-INF/MANIFEST.MF
	rm -f one-jar/boot-manifest.mf
	(grep Main-Class: one-jar/META-INF/MANIFEST.MF; grep Main-Class: META-INF/MANIFEST.MF | sed -e 's,^,One-Jar-,') > one-jar/boot-manifest.mf

one-jar/lib:
	rm -f one-jar/lib
	(cd one-jar; ln -s ../lib)

clean:
	rm -f one-jar/lib
	rm -f one-jar/boot-manifest.mf
	rm -rf one-jar/main
	rm -rf bin/info
	rm -f jTrapKATEditor.jar
