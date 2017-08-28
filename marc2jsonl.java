import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.json.MarcJsonWriter.*;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.*;
import java.nio.charset.Charset;
import java.text.Normalizer;
import javax.swing.filechooser.*;
import org.apache.commons.cli.*;


public class marc2jsonl {

    public static void main(String args[]) {

        Style styletype=Style.LINES;
        FileInputStream in = null;
        String searchIndex = null;
        String indexType = null;
        String inputFilePath = null;
        String outputFilePath = null;
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
            formatter.printHelp("marc2jsonl",options);
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
                formatter.printHelp("marc2jsonl",options);
                System.exit(1);
                return;
            }
        }
        if(writeToFile) {
            outputFilePath = new File(cmd.getOptionValue("output")).getAbsolutePath();
            try {
                printStream = new PrintStream(new File(FileSystemView.getFileSystemView()
                                                       .getDefaultDirectory().toString()
                                                       + File.separator + outputFilePath));
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
