import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
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
	
	Option indexname = new Option("n","indexname",true,"ElasticSearch Index Name");
	indexname.setRequired(false);
	options.addOption(indexname);

	Option type = new Option("t","type",true,"ElasticSearch type");
	type.setRequired(false);
	options.addOption(type);

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

	boolean compression   = cmd.hasOption("gzip");
	boolean indication    = cmd.hasOption("indexname");
	String inputFilePath  = new File(cmd.getOptionValue("input")).getAbsolutePath();
	String outputFilePath = new File(cmd.getOptionValue("output")).getAbsolutePath() + "/";
	String searchIndex = null;
	String indexType = null;
	if(indication){
		searchIndex    = new String(cmd.getOptionValue("indexname"));
		indexType      = new String(cmd.getOptionValue("type"));
	}
	String gz;
	FileInputStream in = null;

        
	if(compression){
		gz=new String(".gz");
	}else{
		gz=new String("");
	}
	
	int reccount = 500;
	if(cmd.hasOption("records")){
		reccount=Integer.parseInt(cmd.getOptionValue("records"));
	}

	try
	{
        	in = new FileInputStream(inputFilePath);
        } catch (IOException e) {
		e.printStackTrace();
	}

        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        //normalize ANSEL diacritics
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        //split at 500 records, select Elasticsearch buffer size 65536, gzip compression disabled
	if(indication){
		try(
        		MarcJsonWriter writer = new MarcJsonWriter(outputFilePath+"bulk.jsonl.%d"+gz,reccount,
	               	MarcJsonWriter.Style.ELASTICSEARCH_BULK,65536,compression)
		               .setIndex(searchIndex,indexType)){
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
	}else{
		try(
        			MarcJsonWriter writer = new MarcJsonWriter(outputFilePath+"bulk.jsonl.%d"+gz,reccount,
	                	MarcJsonWriter.Style.LINES,65536,compression)
		                .setIndex(searchIndex,indexType)){
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
} 
