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

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.jamespot.glifpix.util.Utils;

public class SampleTagsServer {

	private static Logger logger = Logger.getLogger(SampleTagsServer.class);

	private static void prepareTagsServer(Properties props) throws IOException {

		SampleTagsServiceImpl tsi = new SampleTagsServiceImpl(props);
		tsi.loadResources();
		SampleTagsServiceHandler.setImpl(tsi);
	}

	private static void startServer(Properties props) {

		try {
			Server server = new Server(Integer.parseInt(props.getProperty("tagsServer.port")));

			ContextHandlerCollection contexts = new ContextHandlerCollection();
			server.setHandler(contexts);

			Context api = new Context(contexts, "/api", Context.SESSIONS);
			SampleTagsServiceServlet sApi = new SampleTagsServiceServlet(props);
			api.addServlet(new ServletHolder(sApi), "/*");

			Context html = new Context(contexts, "/html", Context.SESSIONS);
			SampleHtmlTestService hts = new SampleHtmlTestService(props);
			html.addServlet(new ServletHolder(hts), "/*");

			server.start();

		} catch (Exception e) {
			logger.error(e);
			System.exit(-1);
		}
	}

	private static void usage(String errMsg) {
		Utils.usage( SampleTagsServer.class.getCanonicalName(), errMsg);
	}

	public static void main(String[] args) {
		Properties props = Utils.cmdLineParse(SampleTagsServer.class.getCanonicalName(), args);

		try {
			prepareTagsServer(props);
		} catch (Exception e) {
			e.printStackTrace();
			usage("Cannot properly start server : " + e.getMessage());
		}
		startServer(props);

	}

}
