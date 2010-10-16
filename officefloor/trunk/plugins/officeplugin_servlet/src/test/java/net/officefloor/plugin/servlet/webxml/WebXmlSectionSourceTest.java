/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.servlet.webxml;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * Tests the {@link WebXmlSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlSectionSourceTest extends AbstractWebXmlTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(WebXmlSectionSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() throws Exception {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil
				.createSectionDesigner(WebXmlSectionSource.class);
		type.addSectionInput("service", null);
		type.addSectionOutput("unhandled", null, false);
		type.addSectionOutput(ServletException.class.getSimpleName(),
				ServletException.class.getName(), true);
		type.addSectionOutput(IOException.class.getSimpleName(),
				IOException.class.getName(), true);
		type.addSectionObject("SERVLET_SERVER", ServletServer.class.getName());
		type.addSectionObject("HTTP_CONNECTION", ServerHttpConnection.class
				.getName());
		type.addSectionObject("REQUEST_ATTRIBUTES", Map.class.getName());
		type.addSectionObject("HTTP_SESSION", HttpSession.class.getName());
		type.addSectionObject("HTTP_SECURITY", HttpSecurity.class.getName());

		// Validate type
		String webXmlLocation = this.getPackageRelativePath(this.getClass())
				+ "/Type.xml";
		SectionLoaderUtil.validateSectionType(type, WebXmlSectionSource.class,
				webXmlLocation);
	}

	/**
	 * Ensure issue if no {@link Servlet} configured.
	 */
	public void testNoServlet() throws Exception {
		this.doDesignTest("NoServlet.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record no servlet configured
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				designer.addIssue(
						"At least one <servlet/> element must be configured",
						AssetType.WORK, "servlet");
			}
		});
	}

	/**
	 * Ensure issue if no {@link Servlet} mapping configured.
	 */
	public void testNoServletMapping() throws Exception {
		this.doDesignTest("NoServletMapping.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record no servlet configured
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				designer
						.addIssue(
								"At least one <servlet-mapping/> element must be configured",
								AssetType.WORK, "servlet-mapping");
				this.recordHttpServlet("Test", null);
			}
		});
	}

	/**
	 * Ensure can load a single {@link Servlet}.
	 */
	public void testSingleServlet() throws Exception {
		this.doDesignTest("SingleServlet.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record single servlet (with mapping)
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				this.recordHttpServlet("Test", "/", "one", "A", "two", "B");
			}
		});
	}

}