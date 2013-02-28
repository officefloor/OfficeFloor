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
package net.officefloor.eclipse.socket;

import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSenderWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpResponseSenderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSourceExtension extends
		AbstractSocketWorkSourceExtension<Work, HttpResponseSenderWorkSource> {

	/**
	 * Initiate.
	 */
	public HttpResponseSenderWorkSourceExtension() {
		super(HttpResponseSenderWorkSource.class, "Http Response Sender");
	}

	/*
	 * ================ WorkSourceExtension ========================
	 */

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("HTTP Status",
				HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS, "200", page,
				context, null);
		SourceExtensionUtil
				.createPropertyText(
						"HTTP Content File",
						HttpResponseSenderWorkSource.PROPERTY_HTTP_RESPONSE_CONTENT_FILE,
						"", page, context, null);
	}

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Should only be the one task

		// Obtain the status
		String status = context.getPropertyList().getPropertyValue(
				HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS,
				"<not specified>");

		// Provide documentation
		return "Send the " + HttpResponse.class.getSimpleName()
				+ " to the client with status " + status;
	}

}