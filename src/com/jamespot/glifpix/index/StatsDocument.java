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
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

public class StatsDocument extends StoredDocument {

	private static Logger logger = Logger.getLogger(StatsDocument.class);

	private boolean initializedDocument;
	private StatsDocument() {
		super();
		initializedDocument = false;
	}

	private StatsDocument(Document d) {
		_luceneDocument = d;
		initializedDocument = true;
	}

	public static StatsDocument create(String lng, int lineNumber, Map<Integer, Integer> lengthMap, String time) throws IOException, NoSuchAlgorithmException {
		StatsDocument toRet = new StatsDocument();
		Document statsDoc = new Document();

		statsDoc.add(new Field("lng", lng, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		statsDoc.add(new Field("nbElements", Integer.toString(lineNumber), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		statsDoc.add(new Field("time", time, Store.YES, Index.NOT_ANALYZED_NO_NORMS));

		for (Map.Entry<Integer, Integer> entry : lengthMap.entrySet()) {
			statsDoc.add(new Field("len_" + entry.getKey(), entry.getValue().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
			logger.info("len_" + entry.getKey() + "  : " + entry.getValue().toString());
		}
		return toRet;
	}

	protected static StatsDocument read(IndexReader ir) throws IOException {
		TermEnum te = ir.terms(new Term("nbElements"));
		if (te.term().field().equalsIgnoreCase("nbElements")) {
			TermDocs td = ir.termDocs(te.term());
			if (td.next()) {
				int idDoc = td.doc();

				return new StatsDocument(ir.document(idDoc));
			}
			throw new IOException("No readable StatsDocument");
		}
		throw new IOException("No StatsDocument found");
	}

	public String getField(String field) throws IOException {
		if (initializedDocument) {
			return _luceneDocument.get(field);
		} else {
			throw new IOException("StatsDocument not initialized");
		}
	}

	public String getLng() throws IOException {
		return getField("lng");
	}

	public String getTime() throws IOException {
		return getField("time");
	}

	public String getNbElements() throws IOException {
		return getField("nbElements");
	}
}
