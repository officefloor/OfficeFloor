package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link ManagedObjectSource} for the {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpResourceStoreManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link HttpResourceStore}.
	 */
	private final HttpResourceStore store;

	/**
	 * Instantiate.
	 * 
	 * @param store
	 *            {@link HttpResourceStore}.
	 */
	public HttpResourceStoreManagedObjectSource(HttpResourceStore store) {
		this.store = store;
	}

	/*
	 * ================ ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpResourceStore.class);
		context.setManagedObjectClass(this.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * =================== ManagedObject ======================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.store;
	}

}