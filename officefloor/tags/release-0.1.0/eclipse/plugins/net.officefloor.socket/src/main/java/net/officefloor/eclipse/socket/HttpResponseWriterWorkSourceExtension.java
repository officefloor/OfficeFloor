/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseWriterWork;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseWriterWorkSource;

/**
 * {@link WorkSourceExtension} for the {@link HttpResponseWriter}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterWorkSourceExtension
		implements
		WorkSourceExtension<HttpResponseWriterWork, HttpResponseWriterWorkSource> {

	/*
	 * ==================== WorkSourceExtension ===========================
	 */

	@Override
	public Class<HttpResponseWriterWorkSource> getWorkSourceClass() {
		return HttpResponseWriterWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP Response Writer";
	}

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {
		// No specification required
		SourceExtensionUtil.loadPropertyLayout(page);
		new Label(page, SWT.NONE).setText("No properties required");
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		return "HttpResponseWriter";
	}

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Should only be the one task

		// Return documentation
		return "Writes the " + HttpFile.class.getSimpleName() + " to the "
				+ HttpResponse.class.getSimpleName();
	}

}