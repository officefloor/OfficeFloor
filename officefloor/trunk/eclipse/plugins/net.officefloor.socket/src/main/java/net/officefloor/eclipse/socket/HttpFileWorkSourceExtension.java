/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.plugin.work.http.file.HttpFileTask;
import net.officefloor.plugin.work.http.file.HttpFileWorkSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link WorkSourceExtension} for the {@link HttpFileWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSourceExtension implements
		WorkSourceExtension<HttpFileTask, HttpFileWorkSource>,
		ExtensionClasspathProvider {

	/*
	 * ================ WorkLoaderExtension ====================
	 */

	@Override
	public Class<HttpFileWorkSource> getWorkSourceClass() {
		return HttpFileWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP File";
	}

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Obtain the properties
		PropertyList properties = context.getPropertyList();

		// Obtain the package prefix and provide default value
		final Property packagePrefixProperty = properties
				.getOrAddProperty(HttpFileWorkSource.PACKAGE_PREFIX_PROPERTY_NAME);
		if (EclipseUtil.isBlank(packagePrefixProperty.getValue())) {
			packagePrefixProperty.setValue("html");
		}

		// Obtain the default index file name
		final Property defaultIndexFileNameProperty = properties
				.getOrAddProperty(HttpFileWorkSource.DEFAULT_INDEX_FILE_PROPETY_NAME);
		if (EclipseUtil.isBlank(defaultIndexFileNameProperty.getValue())) {
			defaultIndexFileNameProperty.setValue("index.html");
		}

		// Provide inputs for properties
		page.setLayout(new GridLayout(2, false));
		new Label(page, SWT.NONE).setText("Package prefix: ");
		final Text packagePrefix = new Text(page, SWT.BORDER);
		packagePrefix.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		packagePrefix.setText(packagePrefixProperty.getValue());
		packagePrefix.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Change property and notify of change
				packagePrefixProperty.setValue(packagePrefix.getText());
				context.notifyPropertiesChanged();
			}
		});
		new Label(page, SWT.NONE).setText("Default index file name: ");
		final Text defaultIndexFileName = new Text(page, SWT.BORDER);
		defaultIndexFileName.setLayoutData(new GridData(SWT.FILL,
				SWT.BEGINNING, true, false));
		defaultIndexFileName.setText(defaultIndexFileNameProperty.getValue());
		defaultIndexFileName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Change property and notify of change
				defaultIndexFileNameProperty.setValue(defaultIndexFileName
						.getText());
				context.notifyPropertiesChanged();
			}
		});
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		return "HttpFile";
	}

	/*
	 * =================== ExtensionClasspathProvider =====================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				HttpFileWorkSource.class) };
	}

}