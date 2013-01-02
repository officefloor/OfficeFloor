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
package net.officefloor.plugin.servlet.mapping;

import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.container.HttpServletServicer;

/**
 * Tests the {@link ServicerMapper} as per &quot;Request Path Elements&quot; of
 * the {@link Servlet} specification.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletRequestPathElementsTest extends
		AbstractServicerMapperTestCase {

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer lawn = new MockServicer("lawn", "/lawn/*");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer garden = new MockServicer("garden", "/garden/*");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer jsp = new MockServicer("jsp", "*.jsp");

	/**
	 * {@link HttpServletServicer}.
	 */
	private final HttpServletServicer any = new MockServicer("default", "/");

	/**
	 * {@link ServicerMapper} to test.
	 */
	private final ServicerMapper mapper = new ServicerMapperImpl(this.lawn,
			this.garden, this.jsp, this.any);

	/**
	 * Ensure maps /lawn/index.html .
	 */
	public void test_lawn_index_html() {
		ServicerMapping mapping = this.mapper.mapPath("/lawn/index.html");
		assertMapping(mapping, this.lawn, "/lawn", "/index.html", null);
	}

	/**
	 * Ensure maps /garden/implements .
	 */
	public void test_garden_implements() {
		ServicerMapping mapping = this.mapper.mapPath("/garden/implements");
		assertMapping(mapping, this.garden, "/garden", "/implements", null);
	}

	/**
	 * Ensure maps /help/feedback.jsp .
	 */
	public void test_help_feedback_jsp() {
		ServicerMapping mapping = this.mapper.mapPath("/help/feedback.jsp");
		assertMapping(mapping, this.jsp, "/help/feedback.jsp", null, null);
	}

}