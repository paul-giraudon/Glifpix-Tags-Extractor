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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class ResourceStore extends DocumentStore {
	
	private Set<Long> _crcs;
	
	private ResourceStore(String path, boolean mode) throws IOException
	{
		super(path, mode);
	}
	
	static public ResourceStore open(String path) throws IOException {
		ResourceStore toRet = new ResourceStore(path, false);
		toRet.loadCRCs();
		return toRet;
	}

	
	static public ResourceStore openForClean(String path) throws IOException {
		ResourceStore toRet = new ResourceStore(path, false);
		return toRet;
	}
	
	
	static public ResourceStore create(String path) throws IOException {
		return new ResourceStore(path, true);
	}


	public StatsDocument getStatsDocument() throws IOException {
		if (readMode) {
			return StatsDocument.read(ir);
		} else {
			throw new IOException("Resource store open in write mode");
		}
	}

	public Set<Long> getCRCs() throws IOException {
		if (readMode) {
			return _crcs; 
		} else {
			throw new IOException("Resource store open in write mode");
		}
	}
	
	public String getTag(String token) throws IOException
	{
		if (readMode) {
			return ResourceDocument.getLiteral(ir, token);
		}
		else
		{
			throw new IOException("Resource store open in write mode");
		}
		
	}
	
	private void loadCRCs() throws IOException
	{
		if (readMode)
		{
			_crcs = ResourceDocument.getCRCs(ir);
		}
	}
	public void deleteTag(String tag) throws IOException
	{
		ResourceDocument.deleteResource(ir, tag);
	}
}
