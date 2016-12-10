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
package net.officefloor.plugin.woof.servlet;

/**
 * Mock logic for the templates.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLogic {

	/**
	 * Mock content for the template.
	 */
	public static class MockContent {

		private String text;

		public MockContent(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Obtains the data for the template.
	 * 
	 * @param dependency
	 *            {@link MockDependency}.
	 * @return {@link MockContent} for rendering template.
	 */
	public MockContent getTemplateData(MockDependency dependency) {
		Thread thread = Thread.currentThread();
		return new MockContent(dependency.getMessage() + " " + thread.getName());
	}

}