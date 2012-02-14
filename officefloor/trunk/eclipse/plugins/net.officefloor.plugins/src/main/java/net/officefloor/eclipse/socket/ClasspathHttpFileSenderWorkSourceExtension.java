/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileFactoryWorkSource;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileSenderWorkSource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for {@link ClasspathHttpFileSenderWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileSenderWorkSourceExtension
		extends
		AbstractSocketWorkSourceExtension<HttpFileFactoryTask<None>, ClasspathHttpFileSenderWorkSource> {

	/**
	 * Initiate.
	 */
	public ClasspathHttpFileSenderWorkSourceExtension() {
		super(ClasspathHttpFileSenderWorkSource.class, "Send Http File");
	}

	/*
	 * =================== WorkSourceExtension ===============================
	 */

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("Package prefix",
				ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
				"html", page, context, null);
		SourceExtensionUtil.createPropertyText("Directory index file name",
				ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
				"index.html", page, context, null);
		SourceExtensionUtil
				.createPropertyText(
						"Resource not found content path",
						ClasspathHttpFileSenderWorkSource.PROPERTY_NOT_FOUND_FILE_PATH,
						"FileNotFound.html", page, context, null);
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
		return "Sends the "
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