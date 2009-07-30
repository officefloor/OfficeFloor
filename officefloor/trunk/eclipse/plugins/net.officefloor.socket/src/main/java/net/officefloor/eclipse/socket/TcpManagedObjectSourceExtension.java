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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.tcp.source.TcpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.tcp.source.TcpServer.TcpServerFlows;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link TcpServerSocketManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, TcpServerFlows, TcpServerSocketManagedObjectSource>,
		ExtensionClasspathProvider {

	@Override
	public Class<TcpServerSocketManagedObjectSource> getManagedObjectSourceClass() {
		return TcpServerSocketManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "TCP";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {

		// Specify layout of page
		page.setLayout(new GridLayout(2, false));

		// Provide the properties
		SourceExtensionUtil.createPropertyDisplay("Port: ",
				TcpServerSocketManagedObjectSource.PROPERTY_PORT, "80", page,
				context);
		SourceExtensionUtil.createPropertyDisplay("Buffer size: ",
				TcpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
				"1024", page, context);
		SourceExtensionUtil
				.createPropertyDisplay(
						"Maximum connections per listener: ",
						TcpServerSocketManagedObjectSource.PROPERTY_MAXIMUM_CONNECTIONS_PER_LISTENER,
						"64", page, context);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "TCP";
	}

	/*
	 * ========================== ExtensionClasspathProvider =================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				TcpServerSocketManagedObjectSource.class) };
	}

}