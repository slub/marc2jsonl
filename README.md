# marc2jsonl

simple tool to convert (binary) MARC21, MarcXML or MabXML files to line-delimited JSON which can also be used to index data into ElasticSearch. It makes use of xbib/marc Java library.

dependencies:

* https://github.com/xbib/marc/
* http://commons.apache.org/proper/commons-cli/

build:

    mvn clean package

or

    make

install:

    su
    make install

run:

    marc2jsonl <OPTARG>

valid options are:

-i --input:	(binary) MARC21, MarcXML or MabXML input file

-o --output: 	output directory

-mabxml --mabxmlinput:   input is MabXML

-marcxml --marcxmlinput:   input is MarcXML

-n --indexname: name of the ElasticSearch Index

-t --type:	name of the ElasticSearch datatype


examples:

    marc2jsonl -i input.mrc -o output.jsonl -n test -t marc

transformes the marc data from input.mrc to line-delimited json and saves to the file output.jsonl


    marc2jsonl < input.mrc | esbulk -index test -type marc

transforms the marc data from stdin and pipes it to esbulk (https://github.com/miku/esbulk) which indexes the data and uploads it to an ElasticSearch-Server

    pv input.mrc | marc2jsonl > output.jsonl

watch the progress with pipeview (http://www.ivarch.com/programs/pv.shtml) and transform the marc data from input.mrc to the file output.jsonl
