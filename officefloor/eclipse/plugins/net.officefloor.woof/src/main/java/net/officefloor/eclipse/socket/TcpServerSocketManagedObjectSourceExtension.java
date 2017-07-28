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

import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.tcp.source.TcpServerSocketManagedObjectSource;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link TcpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpServerSocketManagedObjectSourceExtension
		implements ManagedObjectSourceExtension<None, Indexed, TcpServerSocketManagedObjectSource> {

	@Override
	public Class<TcpServerSocketManagedObjectSource> getManagedObjectSourceClass() {
		return TcpServerSocketManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "TCP";
	}

	@Override
	public void createControl(Composite page, ManagedObjectSourceExtensionContext context) {

		// Specify layout of page
		SourceExtensionUtil.loadPropertyLayout(page);

		// Provide the properties
		SourceExtensionUtil.createPropertyText("Port", TcpServerSocketManagedObjectSource.PROPERTY_PORT, "80", page,
				context, null);
		SourceExtensionUtil.createPropertyText("Send buffer size",
				TcpServerSocketManagedObjectSource.PROPERTY_SEND_BUFFER_SIZE, "8192", page, context, null);
		SourceExtensionUtil.createPropertyText("Receive buffer size",
				TcpServerSocketManagedObjectSource.PROPERTY_RECEIVE_BUFFER_SIZE, "8192", page, context, null);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "TCP";
	}

}