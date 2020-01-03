package net.officefloor.spring;

import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Spring bean {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBeanManagedObjectSource extends AbstractManagedObjectSource<Indexed, None>
		implements ManagedObject, CoordinatingManagedObject<Indexed> {

	/**
	 * Name of the Spring bean.
	 */
	private final String beanName;

	/**
	 * Object type expected from Spring.
	 */
	private final Class<?> objectType;

	/**
	 * Spring {@link ConfigurableApplicationContext}.
	 */
	private final ConfigurableApplicationContext springContext;

	/**
	 * {@link SpringDependency} instances.
	 */
	private final SpringDependency[] springDependencies;

	/**
	 * Instantiate.
	 * 
	 * @param beanName           Name of the Spring bean.
	 * @param objectType         Object type expected from Spring.
	 * @param springContext      Spring {@link ConfigurableApplicationContext}.
	 * @param springDependencies {@link SpringDependency} instances.
	 */
	public SpringBeanManagedObjectSource(String beanName, Class<?> objectType,
			ConfigurableApplicationContext springContext, SpringDependency[] springDependencies) {
		this.beanName = beanName;
		this.objectType = objectType;
		this.springContext = springContext;
		this.springDependencies = springDependencies;
	}

	/*
	 * ===================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {

		// Configure meta-data
		context.setObjectClass(this.objectType);
		context.setManagedObjectClass(this.getClass());

		// Provide extension
		context.addManagedObjectExtension((Class) this.objectType, (mo) -> mo.getObject());

		// Load the dependencies
		for (SpringDependency springDependency : this.springDependencies) {
			context.addDependency(springDependency.getObjectType()).setTypeQualifier(springDependency.getQualifier());
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================= CoordinatingManagedObject ========================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
		// objects via supplier thread local
	}

	/*
	 * ======================== ManagedObject ==============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.springContext.getBean(this.beanName);
	}

}