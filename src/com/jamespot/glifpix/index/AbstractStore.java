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

public class AbstractStore extends DocumentStore {

	private AbstractStore(String path, boolean writemode) throws IOException {
		super(path, writemode);
	}

	static public AbstractStore open(String path) throws IOException {
		return new AbstractStore(path, false);
	}

	static public AbstractStore create(String path) throws IOException {
		return new AbstractStore(path, true);
	}
	
	public float getTagWeight(String token) throws IOException
	{
		if (readMode) {
			return AbstractDocument.getTagWeight(ir, token);
		}
		else
		{
			throw new IOException("Resource store open in write mode");
		}
	}
}
