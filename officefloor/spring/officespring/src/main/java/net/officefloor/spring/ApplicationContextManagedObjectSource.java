package net.officefloor.spring;

import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link ConfigurableApplicationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ApplicationContextManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private final ConfigurableApplicationContext applicationContext;

	/**
	 * Instantiate.
	 * 
	 * @param applicationContext {@link ConfigurableApplicationContext}.
	 */
	public ApplicationContextManagedObjectSource(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/*
	 * ================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(this.applicationContext.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ===================== ManagedObject =======================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.applicationContext;
	}

}