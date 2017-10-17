package de.slubdresden.marc2jsonl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.Normalizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.json.MarcJsonWriter.Style;
import org.xbib.marc.transformer.value.MarcValueTransformers;


public class Marc2JSONL {

    public static void main(String args[]) {

        Style styletype=Style.LINES;
        FileInputStream in = null;
        String searchIndex = null;
        String indexType = null;
        String inputFilePath = null;
        File outputFile = null;
        PrintStream printStream = null;
        Options options = new Options();

        Option input = new Option("i", "input",true,"input file path");
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o","output",true,"output directory");
        output.setRequired(false);
        options.addOption(output);

        Option indexname = new Option("n","indexname",true,"ElasticSearch Index Name");
        indexname.setRequired(false);
        options.addOption(indexname);

        Option type = new Option("t","type",true,"ElasticSearch type");
        type.setRequired(false);
        options.addOption(type);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options,args);
        } catch(ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Marc2JSONL",options);
            System.exit(1);
            return;
        }

        boolean writeToFile   = cmd.hasOption("output");
        boolean readFromFile  = cmd.hasOption("input");
        boolean indication    = cmd.hasOption("indexname");
        
        if(readFromFile) {
            try{
            inputFilePath  = new File(cmd.getOptionValue("input")).getAbsolutePath();
            in = new FileInputStream(inputFilePath);
            System.setIn(in);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("Marc2JSONL",options);
                System.exit(1);
                return;
            }
        }
        if(writeToFile) {
            try {
            outputFile = new File(cmd.getOptionValue("output"));
            outputFile.createNewFile();
            FileOutputStream oFile = new FileOutputStream(outputFile,false);
            printStream = new PrintStream(oFile);
                System.setOut(printStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            printStream = new PrintStream(System.out);
        }
        
        if(indication) {
            searchIndex    = new String(cmd.getOptionValue("indexname"));
            indexType      = new String(cmd.getOptionValue("type"));
            styletype=Style.ELASTICSEARCH_BULK;
        }

        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        //normalize ANSEL diacritics
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        try(
                MarcJsonWriter writer = new MarcJsonWriter(printStream,65536,styletype)
            .setIndex(searchIndex,indexType)) {
            writer.setMarcValueTransformers(marcValueTransformers);
            Marc.builder()
            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            .setInputStream(System.in)
            .setCharset(Charset.forName("UTF-8"))
            .setMarcListener(writer)
            .build()
            .writeCollection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
