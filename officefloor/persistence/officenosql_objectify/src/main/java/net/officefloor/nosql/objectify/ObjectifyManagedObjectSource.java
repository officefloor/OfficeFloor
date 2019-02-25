package net.officefloor.nosql.objectify;

import java.util.HashSet;
import java.util.Set;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.util.Closeable;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide {@link Objectify}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * <p>
	 * {@link Property} name for the comma separate list of
	 * {@link ObjectifyEntityLocator} {@link Class} names.
	 * <p>
	 * {@link ObjectifyEntityLocator} instances configured are instantiated by
	 * default constructors.
	 */
	public static final String PROPERTY_ENTITY_LOCATORS = "objectify.entity.locators";

	/**
	 * Default {@link ObjectifyFactoryManufacturer}.
	 */
	private static final ObjectifyFactoryManufacturer DEFAULT_MANUFACTURER = () -> new ObjectifyFactory();

	/**
	 * {@link ObjectifyFactoryManufacturer}.
	 */
	private static ObjectifyFactoryManufacturer objectifyFactoryManufacturer = DEFAULT_MANUFACTURER;

	/**
	 * Creates the {@link ObjectifyFactory}.
	 */
	@FunctionalInterface
	public static interface ObjectifyFactoryManufacturer {

		/**
		 * Creates the {@link ObjectifyFactory}.
		 * 
		 * @return {@link ObjectifyFactory}.
		 */
		ObjectifyFactory createObjectifyFactory();
	}

	/**
	 * Specifies the {@link ObjectifyFactoryManufacturer}.
	 * 
	 * @param manufacturer {@link ObjectifyFactoryManufacturer}.
	 */
	public static void setObjectifyFactoryManufacturer(ObjectifyFactoryManufacturer manufacturer) {
		objectifyFactoryManufacturer = (manufacturer != null) ? manufacturer : DEFAULT_MANUFACTURER;
	}

	/**
	 * {@link Objectify} {@link Entity} types.
	 */
	private Class<?>[] entityTypes;

	/*
	 * ================= ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> sourceContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(Objectify.class);

		// Load the entity types
		Set<Class<?>> entityTypes = new HashSet<>();

		// Load from property configurations
		String propertyEntityLocators = sourceContext.getProperty(PROPERTY_ENTITY_LOCATORS, null);
		if (propertyEntityLocators != null) {
			NEXT_LOCATOR: for (String entityLocatorClassName : propertyEntityLocators.split(",")) {

				// Ignore if no class name
				if (CompileUtil.isBlank(entityLocatorClassName)) {
					continue NEXT_LOCATOR;
				}

				// Include the entity class
				Class<?> entityLocatorClass = sourceContext.loadClass(entityLocatorClassName.trim());
				entityTypes.add(entityLocatorClass);
			}
		}

		// Capture the entity types
		this.entityTypes = entityTypes.toArray(new Class[entityTypes.size()]);

		// Recycle the Objectify
		context.getManagedObjectSourceContext().getRecycleFunction(() -> (recyleContext) -> {
			RecycleManagedObjectParameter<ObjectifyManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(recyleContext);
			recycle.getManagedObject().closable.close();
			return null;
		}).linkParameter(0, RecycleManagedObjectParameter.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Initialise
		ObjectifyService.init(objectifyFactoryManufacturer.createObjectifyFactory());

		// Register the entity types
		for (Class<?> entityType : this.entityTypes) {
			ObjectifyService.register(entityType);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		Closeable closeable = ObjectifyService.begin();
		return new ObjectifyManagedObject(ObjectifyService.ofy(), closeable);
	}

	/**
	 * {@link ManagedObject} for {@link Objectify}.
	 */
	private static class ObjectifyManagedObject implements ManagedObject {

		private final Objectify objectify;

		private final Closeable closable;

		private ObjectifyManagedObject(Objectify objectify, Closeable closable) {
			this.objectify = objectify;
			this.closable = closable;
		}

		/*
		 * ================= ManagedObject ======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.objectify;
		}
	}

}