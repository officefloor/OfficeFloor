/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.security.secure;

import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Abstract functionality for providing {@link HttpSecurity} on accessing a
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSecureAsyncManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
		extends AbstractAsyncManagedObjectSource<O, F> {

	protected interface InsecureMetaDataContext<O extends Enum<O>, F extends Enum<F>> extends MetaDataContext<O, F> {

		DependencyLabeller<O> addHttpAccessControlDependency();

		DependencyLabeller<O> addHttpAccessControlDependency(O key);
	}

	/**
	 * Loads the insecure specification.
	 * 
	 * @param context
	 *            {@link SpecificationContext}.
	 */
	protected abstract void loadInsecureSpecification(SpecificationContext context);

	/**
	 * Loads the insecure meta-data.
	 * 
	 * @param context
	 *            {@link InsecureMetaDataContext}.
	 * @throws Exception
	 *             If fails to load insecure meta-data.
	 */
	protected abstract void loadInsecureMetaData(InsecureMetaDataContext<O, F> context) throws Exception;

	/**
	 * Loads the insecure {@link ManagedObject}.
	 * 
	 * @param user
	 *            {@link ManagedObjectUser}.
	 */
	protected abstract void loadInsecureManagedObject(ManagedObjectUser user);

	/*
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected final void loadSpecification(SpecificationContext context) {
		this.loadInsecureSpecification(context);
	}

	@Override
	protected final void loadMetaData(MetaDataContext<O, F> context) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public final void sourceManagedObject(ManagedObjectUser user) {
	}

	/**
	 * Wrapper on {@link MetaDataContext} to provide enable the
	 * {@link SecureManagedObject} to wrap the insecure {@link ManagedObject}.
	 */
	private class InsecureMetaDataContextImpl implements MetaDataContext<O, F> {

		/*
		 * ==================== MetaDataContext =============================
		 */

		@Override
		public ManagedObjectSourceContext<F> getManagedObjectSourceContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setObjectClass(Class<?> objectClass) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass) {
			// TODO Auto-generated method stub

		}

		@Override
		public DependencyLabeller addDependency(O key, Class<?> dependencyType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DependencyLabeller addDependency(Class<?> dependencyType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Labeller addFlow(F key, Class<?> argumentType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Labeller addFlow(Class<?> argumentType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <E> void addManagedObjectExtensionInterface(Class<E> interfaceType,
				ExtensionInterfaceFactory<E> extensionInterfaceFactory) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * Secure wrapper around the {@link ManagedObject}.
	 */
	private class SecureManagedObject
			implements ProcessAwareManagedObject, AsynchronousManagedObject, CoordinatingManagedObject<O> {

		/*
		 * ================== ManagedObject ===============================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			// TODO Auto-generated method stub

		}

		@Override
		public void loadObjects(ObjectRegistry<O> registry) throws Throwable {
			// TODO Auto-generated method stub

		}

		@Override
		public Object getObject() throws Throwable {
			// TODO Auto-generated method stub
			return null;
		}
	}

}