/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container.source;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.jasper.servlet.JspServlet;

/**
 * Tests the {@link JspWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JspWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(JspWorkSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Factory
		HttpServletTask factory = new HttpServletTask("JSP", new JspServlet(),
				new HashMap<String, String>());

		// Create the expected type
		WorkTypeBuilder<HttpServletTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<DependencyKeys, None> task = type.addTaskType(
				"service", factory, DependencyKeys.class, None.class);
		task.setDifferentiator(factory);
		task.addObject(ServicerMapping.class).setKey(
				DependencyKeys.SERVICER_MAPPING);
		task.addObject(OfficeServletContext.class).setKey(
				DependencyKeys.OFFICE_SERVLET_CONTEXT);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(HttpRequestState.class).setKey(
				DependencyKeys.REQUEST_ATTRIBUTES);
		task.addObject(HttpSession.class).setKey(DependencyKeys.HTTP_SESSION);
		task.addObject(HttpServletSecurity.class).setKey(
				DependencyKeys.HTTP_SECURITY);
		task.addEscalation(ServletException.class);
		task.addEscalation(IOException.class);

		// Validate type
		WorkType<HttpServletTask> work = WorkLoaderUtil.validateWorkType(type,
				JspWorkSource.class);

		// Ensure match JSP extension
		HttpServletServicer differentiator = (HttpServletServicer) work
				.getTaskTypes()[0].getTaskFactory();
		String[] mappings = differentiator.getServletMappings();
		assertEquals("Incorrect number of mappings", 1, mappings.length);
		assertEquals("Incorrect JSP extension mapping", "*.jsp", mappings[0]);
	}

}