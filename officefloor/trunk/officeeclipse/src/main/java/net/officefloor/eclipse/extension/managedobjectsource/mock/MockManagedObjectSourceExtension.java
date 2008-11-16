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
package net.officefloor.eclipse.extension.managedobjectsource.mock;

import java.util.List;

import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.MockManagedObjectSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the {@link MockManagedObjectSource}.
 * 
 * @author Daniel
 */
public class MockManagedObjectSourceExtension implements
		ManagedObjectSourceExtension {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getManagedObjectSourceClass()
	 */
	@Override
	public Class<? extends ManagedObjectSource<?, ?>> getManagedObjectSourceClass() {
		return MockManagedObjectSource.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#isUsable()
	 */
	@Override
	public boolean isUsable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "Mock";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * net.officefloor.eclipse.
	 * extension.managedobjectsource.ManagedObjectSourceExtensionContext)
	 */
	@Override
	public List<InitiateProperty> createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {
		// Nothing to initialise as should not be used
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #getSuggestedManagedObjectSourceName(java.util.List)
	 */
	@Override
	public String getSuggestedManagedObjectSourceName(
			List<InitiateProperty> properties) {
		return "Mock";
	}

}
