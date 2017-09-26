# marc2jsonl
simple tool to convert marc21 files to Line-delimited JSON which can be used to index data into ElasticSearch using xbib/marc java-library.

dependencies:
https://github.com/xbib/marc/
http://commons.apache.org/proper/commons-cli/

build:
install apache commons cli java package and xbib/marc package.
modify Makefile to have the proper java classpaths to both packages.
type `make`. install via `make install` and use it in the unix way.

run:

`$ marc2jsonl <OPTARG>`

valid options are:

-i --input:	MARC21 input file

-o --output: 	output directory

-n --indexname: name of the ElasticSearch Index

-t --type:	name of the ElasticSearch datatype


examples:

`$ marc2jsonl -i input.mrc -o output.jsonl -n test -t marc`

transformes the marc data from input.mrc to line-delimited json and saves to the file output.jsonl


`$ marc2jsonl < input.mrc | esbulk -index test -type marc`

transforms the marc data from stdin and pipes it to esbulk (https://github.com/miku/esbulk) which indexes the data and uploads it to an ElasticSearch-Server


`$ pv input.mrc | marc2jsonl > output.jsonl`

watch the progress with pipeview and transform the marc data from input.mrc to the file output.jsonl
