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
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jamespot.glifpix.index.ResourceDocument;
import com.jamespot.glifpix.util.Utils;

public class ResourcesCleaner {

	static public Logger logger = Logger.getLogger(ResourcesCleaner.class);

	private static void usage(String errMsg) {
		Utils.usage( ResourcesCleaner.class.getCanonicalName(), errMsg);
	}

	public static void main(String[] args) {

		Properties props = Utils.cmdLineParse(ResourcesCleaner.class.getCanonicalName(), args);

		
		String home = props.getProperty("glifpix.home");
		String resourceBase = home + "/resources";

		String[] lngs = props.getProperty("res.lngs").split(",");

		for (String lng : lngs) {
			if (props.getProperty("res.lng." + lng).equals("true")) {
				cleanLngResource(resourceBase, lng);
			}
		}


	}
	
	public static void cleanLngResource(String resourceBase, String lng)
	{
		
	}

}
