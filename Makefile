CLASSPATH=../lib/miglayout-4.0-swing.jar:../lib/jna-4.1.0.jar:../lib/jna-platform-4.1.0.jar
SOURCE=info/drealm/scala/*.scala info/drealm/scala/*/*.scala

all: jTrapKATEditor.jar

jTrapKATEditor.jar: main.jar one-jar/boot-manifest.mf one-jar/lib
	rm -f jTrapKATEditor.jar
	jar cmf one-jar/boot-manifest.mf jTrapKATEditor.jar -C one-jar/ .

main.jar: compile
	rm -rf one-jar/main
	mkdir one-jar/main
	jar cmf META-INF/MANIFEST.MF one-jar/main/main.jar -C bin/ . -C src/ info/drealm/scala/Localization.properties

compile:
	rm -rf bin/info
	(cd src; scalac -classpath ${CLASSPATH} -d ../bin -feature -optimise ${SOURCE})

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
