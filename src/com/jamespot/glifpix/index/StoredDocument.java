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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;

public class StoredDocument {

	Document _luceneDocument;

	StoredDocument() {
		_luceneDocument = new Document();

	}

	public Document getLuceneDocument() {
		return _luceneDocument;
	}
	
	protected static String replaceUnicodeStr(String in) {
		Pattern p = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
		Matcher m = p.matcher(in);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String u = m.group(1);
			Integer code = Integer.parseInt(u, 16);
			char c = (char)code.intValue();
			m.appendReplacement(sb, String.valueOf(c));
		}
		m.appendTail(sb);
		return sb.toString();

	}
}
