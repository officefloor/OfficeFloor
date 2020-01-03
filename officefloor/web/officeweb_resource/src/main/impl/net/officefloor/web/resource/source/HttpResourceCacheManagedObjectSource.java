package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.resource.HttpResourceCache;

/**
 * {@link ManagedObjectSource} for the {@link HttpResourceCache}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpResourceCacheManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link HttpResourceCache}.
	 */
	private final HttpResourceCache cache;

	/**
	 * Instantiate.
	 * 
	 * @param cache
	 *            {@link HttpResourceCache}.
	 */
	public HttpResourceCacheManagedObjectSource(HttpResourceCache cache) {
		this.cache = cache;
	}

	/*
	 * ================ ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpResourceCache.class);
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
		return this.cache;
	}

}