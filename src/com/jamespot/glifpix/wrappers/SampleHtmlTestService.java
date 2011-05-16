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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

public class SampleHtmlTestService extends HttpServlet {
	private Properties props;

	private static final long serialVersionUID = -1110981361760251969L;

	public SampleHtmlTestService(Properties props) {
		super();
		this.props = props;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();

		String htmlPage = FileUtils.readFileToString(new File(props.getProperty("tagsServer.testPage")));

		out.println(htmlPage);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();

		SampleTagsServiceHandler tsh = new SampleTagsServiceHandler();

		out.println("<h1>HtmlTestService</h1>");
		out.println("<table>");
		String[] tags = tsh.getTags(request.getParameter("text"), request.getParameter("lng"), Integer.parseInt(request.getParameter("nbTags")));

		if (tags != null && tags.length > 0) {
			for (int i = 0; i < tags.length; i++) {
				out.println("<tr><td>" + tags[i] + "</td></tr>");
			}

		}
		out.println("</table>");

		out.println("<p><a href='/html/form'>Back</a></p>");
	}
}
