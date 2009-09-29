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
package net.officefloor.eclipse.socket;

import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSenderWorkSource;

/**
 * {@link WorkSourceExtension} for the {@link HttpResponseSenderWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSourceExtension implements
		WorkSourceExtension<Work, HttpResponseSenderWorkSource> {

	/*
	 * ================ WorkSourceExtension ========================
	 */

	@Override
	public Class<HttpResponseSenderWorkSource> getWorkSourceClass() {
		return HttpResponseSenderWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP Response Sender";
	}

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("HTTP Status",
				HttpResponseSenderWorkSource.PROPERTY_HTTP_STATUS, "200", page,
				context);
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		return "HTTP Response Sender";
	}

}