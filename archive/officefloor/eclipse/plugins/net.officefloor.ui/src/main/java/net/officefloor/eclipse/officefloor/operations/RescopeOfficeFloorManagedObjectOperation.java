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
package net.officefloor.eclipse.officefloor.operations;

import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ManagedObjectScopeInput;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectEditPart;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;

/**
 * Re-scopes the {@link OfficeFloorManagedObjectModel}.
 *
 * @author Daniel Sagenschneider
 */
public class RescopeOfficeFloorManagedObjectOperation
		extends AbstractOfficeFloorChangeOperation<OfficeFloorManagedObjectEditPart> {

	/**
	 * Initialise.
	 *
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public RescopeOfficeFloorManagedObjectOperation(OfficeFloorChanges officeFloorChanges) {
		super("Rescope Managed Object", OfficeFloorManagedObjectEditPart.class, officeFloorChanges);
	}

	/*
	 * ================= AbstractOfficeFloorChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the managed object to re-scope
		OfficeFloorManagedObjectEditPart editPart = context.getEditPart();
		OfficeFloorManagedObjectModel managedObject = editPart.getCastedModel();

		// Obtain the current scope for the managed object
		ManagedObjectScope currentScope;
		String currentScopeText = managedObject.getManagedObjectScope();
		if (OfficeFloorChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(currentScopeText)) {
			currentScope = ManagedObjectScope.PROCESS;
		} else if (OfficeFloorChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(currentScopeText)) {
			currentScope = ManagedObjectScope.THREAD;
		} else if (OfficeFloorChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(currentScopeText)) {
			currentScope = ManagedObjectScope.FUNCTION;
		} else {
			// Can not determine, so default to process
			currentScope = ManagedObjectScope.PROCESS;
		}

		// Obtain the managed object scope
		ScopeBean bean = new ScopeBean(currentScope);
		BeanDialog dialog = context.getEditPart().createBeanDialog(bean, "X", "Y");
		dialog.registerPropertyInput("Scope", new ManagedObjectScopeInput());
		if (!dialog.populate()) {
			// Cancel, so no change
			return null;
		}

		// Return change to re-scope the managed object
		ManagedObjectScope newScope = bean.getScope();
		return changes.rescopeOfficeFloorManagedObject(managedObject, newScope);
	}

	/**
	 * Bean to be populated with the {@link ManagedObjectScope}.
	 */
	public static class ScopeBean {

		/**
		 * {@link ManagedObjectScope}.
		 */
		private ManagedObjectScope scope;

		/**
		 * Initialise.
		 *
		 * @param scope
		 *            Initial {@link ManagedObjectScope}.
		 */
		public ScopeBean(ManagedObjectScope scope) {
			this.scope = scope;
		}

		/**
		 * Obtains the {@link ManagedObjectScope}.
		 *
		 * @return {@link ManagedObjectScope}.
		 */
		public ManagedObjectScope getScope() {
			return this.scope;
		}

		/**
		 * Specifies the {@link ManagedObjectScope}.
		 *
		 * @param scope
		 *            {@link ManagedObjectScope}.
		 */
		public void setScope(ManagedObjectScope scope) {
			this.scope = scope;
		}
	}
}