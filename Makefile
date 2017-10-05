JAVAC=javac
JFLAGS=-g 
CLASSPATH=/usr/share/java/
INSTALL_PATH=/usr/share/java/
INSTALL_BATCH=/usr/bin/marc2jsonl
default: marc2jsonl.class

marc2jsonl.class:
	${JAVAC} ${JFLAGS} -classpath ${CLASSPATH}:./ marc2jsonl.java

install:
	cp marc2jsonl.class /usr/share/java/marc2jsonl.class
	echo '#!/bin/bash\njava -classpath ${CLASSPATH}:${INSTALL_PATH} marc2jsonl' \$$@\ > ${INSTALL_BATCH}
	chmod +x ${INSTALL_BATCH}

clean:
	rm marc2jsonl.class

uninstall:
	rm ${INSTALL_PATH}marc2jsonl.class
	rm ${INSTALL_BATCH}
