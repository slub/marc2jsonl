JAVAC=javac
JFLAGS=-g
CLASSPATH="./jar/commons-cli-1.3.1.jar:./jar/marc-1.0.12.jar:./"
default: marc2jsonl

marc2jsonl:
	${JAVAC} ${JFLAGS} -Xdiags:verbose -classpath ${CLASSPATH} marc2jsonl.java 
clean:
	rm marc2jsonl.class


