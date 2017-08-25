# marc2jsonl
simple tool to convert marc21 files to Line-delimited JSON which can be used to index data into ElasticSearch using xbib/marc java-library.

dependencies:
https://github.com/xbib/marc/
http://commons.apache.org/proper/commons-cli/

build:
$ javac -classpath ./build/libs/marc-1.0.12.jar:/usr/share/java/commons-cli.jar marc2jsonl.java

run:

$java -classpath ./build/libs/marc-1.0.12.jar:/usr/share/java/commons-cli.jar marc2json <OPTARG>

valid options are:
-i --input:	MARC21 input file
-o --output: 	output directory
-r --records:	number of records per outputfile
-z --gzip:	use gzip compression for outputfiles

