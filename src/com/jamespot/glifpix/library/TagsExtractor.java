package com.jamespot.glifpix.library;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

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

public interface TagsExtractor {
	
	
	
	void loadResources() throws IOException;
	
	/**
	 * Get tags out of content. Calls GlifPix TagsExtractor and returns tags
	 * from content. This function is called by Exalead during the indexing
	 * process.
	 * 
	 * @param content
	 *            Content to be analyzed.
	 * @param lng
	 *            2 characters iso language.
	 * @param maxTags
	 *            Maximum tags to be returned.
	 * @return a list of maximum maxTags tags
	 */
	String[] getTags(String content, String lng, int maxTags);

}
