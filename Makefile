LIBDIR=$(shell cd lib && /bin/pwd)
CLASSPATH=${LIBDIR}/scala-xml.jar:${LIBDIR}/miglayout-4.0-swing.jar:${LIBDIR}/jna-4.1.0.jar:${LIBDIR}/jna-platform-4.1.0.jar
SRC=$(shell cd src/main/scala && find -type f -name '*.scala')
NOTSRC=$(shell cd src/main/resources && find -type f ! -name '*.scala')
VPATH=src/main/scala:src/main/resources
VERSION=$(shell date '+%y-%m%d-%H%M')

jTrapKATEditor.jar: one-jar/main/main.jar one-jar/boot-manifest.mf one-jar/lib
	rm -f jTrapKATEditor.jar
	jar cmf one-jar/boot-manifest.mf jTrapKATEditor.jar -C one-jar/ .

one-jar/main/main.jar: class not-class
	rm -rf one-jar/main
	mkdir one-jar/main
	echo ${VERSION} > bin/info/drealm/scala/version.txt
	jar cmf META-INF/MANIFEST.MF one-jar/main/main.jar -C bin/ .

one-jar/boot-manifest.mf: one-jar/META-INF/MANIFEST.MF META-INF/MANIFEST.MF
	rm -f one-jar/boot-manifest.mf
	(grep Main-Class: one-jar/META-INF/MANIFEST.MF; grep Main-Class: META-INF/MANIFEST.MF | sed -e 's,^,One-Jar-,') > one-jar/boot-manifest.mf

one-jar/lib:
	rm -f one-jar/lib
	(cd one-jar; ln -s ../lib)

class: bin ${SRC}
	(cd src/main/scala && scalac -classpath ${CLASSPATH} -d ../../../bin -deprecation -feature -optimise ${SRC}) && touch $@

not-class: bin ${NOTSRC}
	tar cf - -C src/main/resources ${NOTSRC} | tar xf - -C bin && touch $@

bin:
	mkdir bin

clean:
	rm -f one-jar/boot-manifest.mf
	rm -f one-jar/lib
	rm -rf bin

realclean:
	rm -f jTrapKATEditor.jar
	rm -rf one-jar/main
	rm -f one-jar/boot-manifest.mf
	rm -f one-jar/lib
	rm -f class not-class
	rm -rf bin
