package com.jamespot.glifpix.resources;

/* ----------------------------------------------------------------------------------

 This file is part of GlifPix Tags Extractor.

 GlifPix Tags Extractor is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 GlifPix Tags Extractor is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GlifPix Tags Extractor.  If not, see <http://www.gnu.org/licenses/>.

 Contact : paul<at>jamespot<dot>com

 ---------------------------------------------------------------------------------- */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jamespot.glifpix.index.AbstractDocument;
import com.jamespot.glifpix.index.AbstractStore;
import com.jamespot.glifpix.library.TagsExtractorImpl;
import com.jamespot.glifpix.util.Utils;
import com.sun.org.apache.regexp.internal.RE;

public class AbstractsHandler {
	static public Logger logger = Logger.getLogger(AbstractsHandler.class);

	static RE regExp;

	private static void usage(String errMsg) {
		Utils.usage( AbstractsHandler.class.getCanonicalName(), errMsg);
	}

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {

		Properties props = Utils.cmdLineParse(AbstractsHandler.class.getCanonicalName(), args);
		
		// Read arguments
		String home = props.getProperty("glifpix.home");
		String resourceBase = home + File.separator + "resources";

		// Load a tagExtractor 
		TagsExtractorImpl tagExtractor = new TagsExtractorImpl(props);
		tagExtractor.loadResourcesWithoutAbstracts();

		
		String[] lngs = props.getProperty("res.lngs").split(",");

		for (String lng : lngs) {
			if (lng != null && props.getProperty("res.lng." + lng) != null && props.getProperty("res.lng." + lng).equals("true")) {
				handleLngAbstracts(tagExtractor, resourceBase, lng);
			}
		}
	}

	private static void handleLngAbstracts(TagsExtractorImpl tagExtractor, String resourcePath, String lng) throws CorruptIndexException, LockObtainFailedException,
			IOException {

		// Test arguments semantics
		String inPath = resourcePath + File.separatorChar + "abstracts_" + lng + ".nt";
		File inResource = new File(inPath);
		if (!(inResource.exists() && inResource.canRead())) {
			usage("Can't read " + inPath);
		}

		// Create output directory
		String outPath = resourcePath + File.separatorChar + "indices" + File.separatorChar + "abstracts_" +lng;
		File outIndex = new File(outPath);

		if (outIndex.exists()) {
			try {
				FileUtils.deleteDirectory(outIndex);
			} catch (IOException exc) {
				usage(exc.getMessage());
			}
		}

		if (!outIndex.mkdirs()) {
			usage("Can't create output directory " + outPath);
		}

		
		// Create lucene store for abstracts, indexing tags extracted
		try {
			AbstractStore abstractsStore = AbstractStore.create(outPath);

			long startTime = System.currentTimeMillis();
			// Open inResource and parse it
			Pattern linePattern = Pattern.compile("^<([^>]+)> \"(.*)\"$");

			try {
				BufferedReader input = new BufferedReader(new FileReader(inResource));
				int lineNumber = 0;
				int nbNotMatched = 0;

				try {
					String line = null;
					while ((line = input.readLine()) != null) {
						lineNumber += 1;
						if (lineNumber % 100000 == 0) {
							logger.info(lineNumber + " lines parsed so far, " + nbNotMatched + " with no match");
						}
						Matcher lineMatcher = linePattern.matcher(line);
						if (lineMatcher.matches()) {
							String literal = lineMatcher.group(2);
							try {
								AbstractDocument aDoc = AbstractDocument.create(tagExtractor, literal, lng);
								abstractsStore.addDocument(aDoc);

							} catch (Exception e) {
								nbNotMatched += 1;
								logger.info("error handling :" + line + "  " + e.getMessage());
							}
						} else {
							nbNotMatched += 1;
							logger.info("no match for :" + line);
						}
					}
				} finally {
					input.close();
				}
				long endTime = System.currentTimeMillis();

				logger.info("------------------------------------------------------");

				logger.info("Total lines : " + lineNumber + " in " + Long.toString(endTime - startTime) + " ms");

			} catch (IOException ex) {
				ex.printStackTrace();
			}

			abstractsStore.close();

		} catch (Exception e) {
			usage("Can't create index in " + outPath);
		}
	}

}
