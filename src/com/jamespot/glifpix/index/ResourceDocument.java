package com.jamespot.glifpix.index;

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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.LockObtainFailedException;

import com.jamespot.glifpix.library.ContentAnalyzer;
import com.jamespot.glifpix.resources.ResourcesHandler;
import com.jamespot.glifpix.util.Utils;

public class ResourceDocument extends StoredDocument {

	static public Logger logger = Logger.getLogger(ResourceDocument.class);
	int _resourceLength;

	private ResourceDocument() {
		super();
	}

	public static ResourceDocument create(String name, String literal) throws IOException {
		ResourceDocument toRet = new ResourceDocument();
		toRet.addNameField(name);
		toRet.addLiteralField(literal);
		return toRet;
	}

	private void addNameField(String name) {
		// _luceneDocument.add(new Field("name", name, Store.YES,
		// Index.NOT_ANALYZED_NO_NORMS));
	}

	private void addLiteralField(String literal) throws IOException {
		_luceneDocument.add(new Field("literal", replaceUnicodeStr(literal), Store.YES, Index.NOT_ANALYZED_NO_NORMS));

		String coolLiteral = literal.replaceAll("\\\"", "");
		coolLiteral = replaceUnicodeStr(coolLiteral);

		Analyzer resAnalyzer = new ContentAnalyzer();
		TokenStream ts = resAnalyzer.tokenStream("dummyField", new StringReader(coolLiteral));

		TermAttribute termAttribute = ts.addAttribute(TermAttribute.class);

		int length = 0;
		StringBuffer sb = new StringBuffer();
		while (ts.incrementToken()) {
			sb.append("_" + termAttribute.term());
			length++;
		}
		sb.insert(0, length);
		_resourceLength = length;
		ts.end();
		ts.close();

		String finalToken = sb.toString();
		_luceneDocument.add(new Field("token", finalToken, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		_luceneDocument.add(new Field("crc", Utils.getCRC(finalToken), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
	}

	protected static Set<Long> getCRCs(IndexReader ir) throws IOException {
		logger.info("Loading CRCs");
		int nbDocs = ir.numDocs();
		float loadFactor = .75f;
		int initCapacity = ((int) (nbDocs / loadFactor)) + 1000;

		Set<Long> toRet = new HashSet<Long>(initCapacity, loadFactor);
		int nbCrc = 0;
		TermEnum te = ir.terms(new Term("crc"));

		for (boolean hasNext = true; hasNext; hasNext = te.next()) {
			nbCrc++;
			if (nbCrc % 100000 == 0) {
				logger.info(nbCrc + " CRCs loaded");
			}
			Term t = te.term();
			if (!t.field().equalsIgnoreCase("crc"))
				break;
			Long crc = Long.parseLong(t.text());
			toRet.add(crc);
		}
		logger.info("Total CRCs loaded :" + nbCrc);
		return toRet;
	}

	public int getResourceLength() {
		return _resourceLength;
	}

	public static String getLiteral(IndexReader ir, String token) throws IOException {
		TermEnum te = ir.terms(new Term("token", token));
		if (te.term().field().equalsIgnoreCase("token")) {
			TermDocs td = ir.termDocs(te.term());
			if (td.next()) {
				int idDoc = td.doc();
				Document doc = ir.document(idDoc);
				if (doc.get("token").equals(token)) {
					return doc.get("literal");
				}
			}
		}
		return null;
	}
	

	public static void deleteResource(IndexReader ir, String token) throws IOException {
		ir.deleteDocuments(new Term("token", token));
	}
}
