import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;

import org.apache.commons.cli.*;


public class marc2jsonl {

    public static void main(String args[]){
		
	Options options = new Options();
	
	Option input = new Option("i", "input",true,"input file path");
	input.setRequired(true);
	options.addOption(input);
	
	Option output = new Option("o","output",true,"output directory");
	output.setRequired(true);
	options.addOption(output);

	Option comp = new Option("z","gzip",false,"compression");
	comp.setRequired(false);
	options.addOption(comp);

	Option records = new Option("r","records",true,"Records per file");
	records.setRequired(false);
	options.addOption(records);

	CommandLineParser parser = new DefaultParser();
	HelpFormatter formatter = new HelpFormatter();
	CommandLine cmd; 

	try{
		cmd = parser.parse(options,args);
	}catch(ParseException e){
		System.out.println(e.getMessage());
		formatter.printHelp("MarcImport",options);
		System.exit(1);
		return;
	}
	String inputFilePath = cmd.getOptionValue("input");
	String outputFilePath = cmd.getOptionValue("output")+"/";
	boolean compression = cmd.hasOption("gzip");
	int reccount = 500;
	String gz;
        if(compression){
		gz=new String(".gz");
	}else{
		gz=new String("");
	}
	if(cmd.hasOption("records")){
		reccount=Integer.parseInt(cmd.getOptionValue("records"));
	}

        InputStream in = marc2jsonl.class.getResourceAsStream(inputFilePath);
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        //normalize ANSEL diacritics
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        //split at 500 records, select Elasticsearch buffer size 65536, gzip compression disabled
	try(
        		MarcJsonWriter writer = new MarcJsonWriter(outputFilePath+"bulk.jsonl.%d"+gz,reccount,
	                MarcJsonWriter.Style.ELASTICSEARCH_BULK,65536,compression)
	                .setIndex("testindex","testtype")){
		            writer.setMarcValueTransformers(marcValueTransformers);
	        	    Marc.builder()
	                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
	                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
	                    .setInputStream(in)
	                    .setCharset(Charset.forName("UTF-8"))
	                    .setMarcListener(writer)
	                    .build()
	                    .writeCollection();

        } catch (IOException e) {
            e.printStackTrace();
       	}
    }
} 
