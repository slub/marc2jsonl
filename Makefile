INSTALL_PATH=/usr/share/java/
INSTALL_BATCH=/usr/bin/marc2jsonl
MARC2JSONL_JAR=marc2jsonl-0.0.4-SNAPSHOT-onejar.jar
default: compile

compile: 
	mvn clean package

install: 
	cp target/${MARC2JSONL_JAR} ${INSTALL_PATH}${MARC2JSONL_JAR}
	echo '#!/bin/bash\njava -jar ${INSTALL_PATH}${MARC2JSONL_JAR}' \$$@\ > ${INSTALL_BATCH}
	chmod +x ${INSTALL_BATCH}

clean:
	mvn clean

uninstall:
	rm ${INSTALL_PATH}${MARC2JSONL_JAR}
	rm ${INSTALL_BATCH}
