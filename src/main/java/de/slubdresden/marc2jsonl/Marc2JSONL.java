package de.slubdresden.marc2jsonl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
import org.xbib.marc.dialects.mab.xml.MabXMLConstants;
import org.xbib.marc.dialects.mab.xml.MabXMLContentHandler;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.json.MarcJsonWriter.Style;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;
import org.xml.sax.InputSource;


public class Marc2JSONL {

	private static final String INPUT_PARAMETER = "input";
	private static final String OUTPUT_PARAMETER = "output";
	private static final String MABXMLINPUT_PARAMETER = "mabxmlinput";
	private static final String MABINPUT_PARAMETER = "mabinput";
	private static final String INDEXNAME_PARAMETER = "indexname";
	private static final String TYPE_PARAMETER = "type";
	private static final String MARC_2_JSONL_TOOL_NAME = "Marc2JSONL";

	private static final String MAB_XML_FORMAT = "MabXML";
	private static final String MAB_FORMAT = "MAB";
	private static final String MAB_TYPE = "h";

	private static final int BUFFER_SIZE = 65536;
	private static final int FAILED_EXIT_STATUS = 1;

	private static Options createCmdOptions() {

		final Options options = new Options();

		final Option input = new Option("i", INPUT_PARAMETER, true, "input file path");
		input.setRequired(false);
		options.addOption(input);

		final Option output = new Option("o", OUTPUT_PARAMETER, true, "output directory");
		output.setRequired(false);
		options.addOption(output);

		final Option mabxml = new Option("mabxml", MABXMLINPUT_PARAMETER, false, "input is MabXML");
		mabxml.setRequired(false);
		options.addOption(mabxml);

		final Option mab = new Option("mab", MABINPUT_PARAMETER, false, "input is MAB");
		mab.setRequired(false);
		options.addOption(mab);

		final Option indexname = new Option("n", INDEXNAME_PARAMETER, true, "ElasticSearch Index Name");
		indexname.setRequired(false);
		options.addOption(indexname);

		final Option type = new Option("t", TYPE_PARAMETER, true, "ElasticSearch type");
		type.setRequired(false);
		options.addOption(type);

		return options;
	}

	public static void main(final String args[]) {

		Style styletype = Style.LINES;
		FileInputStream in;
		String searchIndex = null;
		String indexType = null;
		String absoluteInputFilePath;
		File outputFile;
		PrintStream printStream;

		final Options options = createCmdOptions();

		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();

		CommandLine cmd;

		try {

			cmd = parser.parse(options, args);
		} catch (final ParseException e) {

			System.out.println(e.getMessage());
			formatter.printHelp(MARC_2_JSONL_TOOL_NAME, options);
			System.exit(FAILED_EXIT_STATUS);

			return;
		}

		final boolean writeToFile = cmd.hasOption(OUTPUT_PARAMETER);
		final boolean readFromFile = cmd.hasOption(INPUT_PARAMETER);
		final boolean mabxmlInput = cmd.hasOption(MABXMLINPUT_PARAMETER);
		final boolean mabInput = cmd.hasOption(MABINPUT_PARAMETER);
		final boolean indication = cmd.hasOption(INDEXNAME_PARAMETER);

		if (readFromFile) {

			try {

				final String inputFilePath = cmd.getOptionValue(INPUT_PARAMETER);
				absoluteInputFilePath = new File(inputFilePath).getAbsolutePath();
				in = new FileInputStream(absoluteInputFilePath);
				System.setIn(in);
			} catch (final FileNotFoundException e) {

				System.out.println(e.getMessage());
				formatter.printHelp(MARC_2_JSONL_TOOL_NAME, options);
				System.exit(FAILED_EXIT_STATUS);

				return;
			}
		}

		if (writeToFile) {

			try {

				final String outputFilePath = cmd.getOptionValue(OUTPUT_PARAMETER);
				outputFile = new File(outputFilePath);
				//outputFile.createNewFile();
				FileOutputStream oFile = new FileOutputStream(outputFile, false);
				printStream = new PrintStream(oFile);
				System.setOut(printStream);
			} catch (final Exception e) {

				System.out.println(e.getMessage());
				formatter.printHelp(MARC_2_JSONL_TOOL_NAME, options);
				System.exit(FAILED_EXIT_STATUS);

				return;
			}
		} else {

			printStream = new PrintStream(System.out);
		}

		if (indication) {

			searchIndex = cmd.getOptionValue(INDEXNAME_PARAMETER);
			indexType = cmd.getOptionValue(TYPE_PARAMETER);
			styletype = Style.ELASTICSEARCH_BULK;
		}

		try {

			//normalize ANSEL diacritics
			final MarcValueTransformers marcValueTransformers = new MarcValueTransformers()
					.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));

			final MarcJsonWriter tempWriter = new MarcJsonWriter(printStream, BUFFER_SIZE, styletype);

			final MarcJsonWriter tempWriter2;

			if (indication) {

				tempWriter2 = tempWriter.setIndex(searchIndex, indexType);
			} else {

				tempWriter2 = tempWriter;
			}

			final Marc.Builder tempBuilder = Marc.builder()
					.setInputStream(System.in)
					.setCharset(StandardCharsets.UTF_8);

			final Marc.Builder builder;

			final MarcJsonWriter writer;

			if (mabxmlInput) {

				writer = tempWriter2;
				writer.beginCollection();

				final MarcContentHandler contentHandler = new MabXMLContentHandler()
						.addNamespace(MabXMLConstants.MABXML_NAMESPACE)
						.setFormat(MAB_XML_FORMAT)
						.setType(MAB_TYPE)
						.setMarcValueTransformers(marcValueTransformers)
						.setMarcListener(writer);

				builder = tempBuilder.setContentHandler(contentHandler);
			} else if (mabInput) {

				writer = tempWriter2.setFormat(MAB_FORMAT)
						.setType(MAB_TYPE);

				builder = tempBuilder
						.setFormat(MAB_FORMAT)
						.setType(MAB_TYPE)
						.setMarcListener(MAB_TYPE, writer);
			} else {

				writer = tempWriter2.setMarcValueTransformers(marcValueTransformers);

				builder = tempBuilder
						.setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
						.setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
						.setMarcListener(writer);
			}

			final Marc marc = builder.build();

			if (mabxmlInput) {

				marc.xmlReader().parse(new InputSource(System.in));

				writer.endCollection();
			} else if (mabInput) {

				marc.writeCollection(MAB_TYPE);
			} else {

				marc.writeCollection();
			}

			writer.close();
		} catch (final IOException e) {

			System.out.println(e.getMessage());
			formatter.printHelp(MARC_2_JSONL_TOOL_NAME, options);
			System.exit(FAILED_EXIT_STATUS);
		}
	}
}
