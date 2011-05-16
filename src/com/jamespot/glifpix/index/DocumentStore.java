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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class DocumentStore {
	IndexWriter iw;
	IndexReader ir;
	boolean writeMode;
	boolean readMode;

	protected DocumentStore(String resourceStoreDir, boolean writeMode) throws CorruptIndexException, LockObtainFailedException, IOException {
		this.writeMode = writeMode;
		this.readMode = !writeMode;

		if (writeMode) {
			ir = null;
			iw = new IndexWriter(new SimpleFSDirectory(new File(resourceStoreDir)), new StandardAnalyzer(Version.LUCENE_30), MaxFieldLength.UNLIMITED);
			iw.setUseCompoundFile(true);
			iw.setRAMBufferSizeMB(256);
		} else {
			iw = null;
			ir = IndexReader.open(new SimpleFSDirectory(new File(resourceStoreDir)));
		}
	}

	static public DocumentStore open(String path) throws CorruptIndexException, LockObtainFailedException, IOException {
		return new DocumentStore(path, false);
	}

	static public DocumentStore create(String path) throws CorruptIndexException, LockObtainFailedException, IOException {
		return new DocumentStore(path, true);
	}

	public void addDocument(StoredDocument d) throws CorruptIndexException, IOException {
		if (writeMode) {
			iw.addDocument(d.getLuceneDocument());
		} else {
			throw new IOException("Resource store open in read mode");
		}
	}

	public void close() throws CorruptIndexException, IOException {
		if (writeMode) {
			iw.commit();
			iw.optimize();
			iw.close();
		} else {
			ir.close();
		}
	}
}
