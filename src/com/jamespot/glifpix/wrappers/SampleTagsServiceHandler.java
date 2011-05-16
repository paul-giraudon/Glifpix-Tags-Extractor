package com.jamespot.glifpix.wrappers;



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

public class SampleTagsServiceHandler {

	private static SampleTagsServiceImpl _sapi = null;
	public SampleTagsServiceHandler() {
	}

	public static void setImpl(SampleTagsServiceImpl api) {
		SampleTagsServiceHandler._sapi = api;
	}

	public String version() {
		return _sapi.version();
	}

	public String[] getTags(String content, String lng, int maxTags) {
		return _sapi.getTags(content, lng, maxTags);
	}
}