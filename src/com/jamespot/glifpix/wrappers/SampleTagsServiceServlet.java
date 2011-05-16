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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.metadata.XmlRpcSystemImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;

public class SampleTagsServiceServlet implements Servlet {

	Properties props;

	private static final long serialVersionUID = 1L;
	private XmlRpcServletServer xmlrpc;

	public SampleTagsServiceServlet(Properties props) {
		super();
		this.props = props;
	}

	public void init(ServletConfig servletConfig) throws ServletException {

		try {
			xmlrpc = new XmlRpcServletServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("tagsService", SampleTagsServiceHandler.class);
			
			XmlRpcSystemImpl.addSystemHandler(phm);
			xmlrpc.setHandlerMapping(phm);

			XmlRpcServerConfigImpl conf = (XmlRpcServerConfigImpl) xmlrpc.getConfig();
			conf.setEnabledForExtensions(true);

		} catch (XmlRpcException e) {
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		xmlrpc.execute((HttpServletRequest) request, (HttpServletResponse) response);
	}

}
