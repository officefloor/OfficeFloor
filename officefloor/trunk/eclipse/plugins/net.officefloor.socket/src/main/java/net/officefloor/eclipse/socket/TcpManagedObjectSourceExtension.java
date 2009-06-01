/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.tcp.TcpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.tcp.TcpServer.TcpServerFlows;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link TcpServerSocketManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, TcpServerFlows, TcpServerSocketManagedObjectSource> {

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
		page.setLayout(new GridLayout());
		new Label(page, SWT.NONE).setText("TODO implement "
				+ this.getClass().getSimpleName());
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "TCP";
	}

}