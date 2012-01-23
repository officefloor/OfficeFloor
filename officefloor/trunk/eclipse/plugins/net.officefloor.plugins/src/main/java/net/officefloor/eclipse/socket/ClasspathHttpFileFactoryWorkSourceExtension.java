/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileFactoryWorkSource;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileFactoryWorkSource.HttpFileFactoryTaskFlows;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the
 * {@link ClasspathHttpFileFactoryWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileFactoryWorkSourceExtension
		extends
		AbstractSocketWorkSourceExtension<HttpFileFactoryTask<HttpFileFactoryTaskFlows>, ClasspathHttpFileFactoryWorkSource> {

	/**
	 * Initiate.
	 */
	public ClasspathHttpFileFactoryWorkSourceExtension() {
		super(ClasspathHttpFileFactoryWorkSource.class, "Get Http File");
	}

	/*
	 * ================ WorkSourceExtension ====================
	 */

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("Package prefix",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"html", page, context, null);
		SourceExtensionUtil.createPropertyText("Directory index file name",
				ClasspathHttpFileFactoryWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html", page, context, null);
	}

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Should always only have the one task

		// Obtain the prefix
		String prefix = context.getPropertyList().getPropertyValue(
				ClasspathHttpFileFactoryWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"<not specified>");

		// Return the documentation
		return "Returns the "
				+ HttpFile.class.getSimpleName()
				+ " from the class path as specified on the request URI of the "
				+ HttpRequest.class.getSimpleName()
				+ "\n\nIn finding the "
				+ HttpFile.class.getSimpleName()
				+ " the prefix '"
				+ prefix
				+ "' is added to the "
				+ HttpRequest.class.getSimpleName()
				+ " request URI to restrict access to full class path resources.";
	}

}