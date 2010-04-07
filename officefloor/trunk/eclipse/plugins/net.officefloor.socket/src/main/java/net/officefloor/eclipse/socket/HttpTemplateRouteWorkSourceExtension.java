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

import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.socket.server.http.template.route.HttpTemplateRouteTask;
import net.officefloor.plugin.socket.server.http.template.route.HttpTemplateRouteWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpTemplateRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateRouteWorkSourceExtension
		extends
		AbstractSocketWorkSourceExtension<HttpTemplateRouteTask, HttpTemplateRouteWorkSource> {

	/**
	 * Initiate.
	 */
	public HttpTemplateRouteWorkSourceExtension() {
		super(HttpTemplateRouteWorkSource.class, "Http Template Route");
	}

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {
		SourceExtensionUtil.informNoPropertiesRequired(page);
	}

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {
		return "Dynamically routes requests to the template handling task";
	}

}