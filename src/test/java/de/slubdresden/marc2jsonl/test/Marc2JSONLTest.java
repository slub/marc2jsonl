package de.slubdresden.marc2jsonl.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slubdresden.marc2jsonl.Marc2JSONL;

public class Marc2JSONLTest {

	private static final Logger LOG = LoggerFactory.getLogger(Marc2JSONLTest.class);

	private static final String INPUT_PARAMETER = "--input";
	private static final String OUTPUT_PARAMETER = "--output";
	private static final String MABXMLINPUT_PARAMETER = "--mabxmlinput";

	private static final String PROJECT_ROOT = System.getProperty("project.root");
	private static final String USER_DIR = System.getProperty("user.dir");
	private static final String ROOT_PATH;

	static {

		if (PROJECT_ROOT != null) {

			ROOT_PATH = PROJECT_ROOT;
		} else if (USER_DIR != null) {

			ROOT_PATH = USER_DIR;
		} else {

			Marc2JSONLTest.LOG.error("could not determine root path - project.root and user.dir is not available");

			ROOT_PATH = "";
		}
	}

	private static final String TEST_RESOURCES_ROOT_PATH =
			ROOT_PATH + File.separator + "src" + File.separator + "test" + File.separator + "resources";
	private static final String DEFAULT_RESULTS_FOLDER = ROOT_PATH + File.separator + "target";

	@Test
	public void testMARCInput() throws IOException, JSONException {

		final File input = new File(this.getClass().getResource("/test-marc.mrc").getFile());
		final String inputFilePath = input.getAbsolutePath();
		final String fileName = "test-marc.ldj";

		final String actualResultFilePath = DEFAULT_RESULTS_FOLDER + File.separator + fileName;
		final String expectedResultFilePath = TEST_RESOURCES_ROOT_PATH + File.separator + fileName;

		final String[] args = new String[]{
				INPUT_PARAMETER,
				inputFilePath,
				OUTPUT_PARAMETER,
				actualResultFilePath
		};

		Marc2JSONL.main(args);

		compareLDJResultFromFile(expectedResultFilePath, actualResultFilePath);
	}

	@Test
	public void testMABXMLInput() throws IOException, JSONException {

		final File input = new File(this.getClass().getResource("/test-mabxml.xml").getFile());
		final String inputFilePath = input.getAbsolutePath();
		final String fileName = "test-mabxml.ldj";

		final String actualResultFilePath = DEFAULT_RESULTS_FOLDER + File.separator + fileName;
		final String expectedResultFilePath = TEST_RESOURCES_ROOT_PATH + File.separator + fileName;

		final String[] args = new String[]{
				MABXMLINPUT_PARAMETER,
				INPUT_PARAMETER,
				inputFilePath,
				OUTPUT_PARAMETER,
				actualResultFilePath
		};

		Marc2JSONL.main(args);

		compareLDJResultFromFile(expectedResultFilePath, actualResultFilePath);
	}

	private static void compareLDJResultFromFile(final String expectedLDJResultFile,
	                                             final String actualLDJResultFile) throws IOException, JSONException {

		final BufferedReader expectedLDJReader = readFile(expectedLDJResultFile);
		final BufferedReader actualLDJReader = readFile(actualLDJResultFile);

		compareLDJResult(expectedLDJReader, actualLDJReader);
	}

	private static void compareLDJResult(final BufferedReader expectedLDJReader,
	                                     final BufferedReader actualLDJReader) throws IOException, JSONException {

		for (String expectedLine; (expectedLine = expectedLDJReader.readLine()) != null; ) {

			final String actuaLine = actualLDJReader.readLine();

			if (actuaLine == null) {

				Assert.assertTrue("actual line-delimited JSON is already empty, but shouldn't be empty.", false);
			}

			JSONAssert.assertEquals(expectedLine, actuaLine, true);
		}

		if (actualLDJReader.readLine() != null) {

			Assert.assertTrue("actual line-delimited JSON has more lines than the expected line-delimited JSON", false);
		}
	}

	private static BufferedReader readFile(final String filePath) throws FileNotFoundException {

		final FileReader fileReader = new FileReader(filePath);

		return new BufferedReader(fileReader);
	}
}
