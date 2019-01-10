package net.officefloor.app.subscription.store;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide {@link Objectify}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyManagedObjectSource extends AbstractManagedObjectSource<None, None> {

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

	/*
	 * ================= ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(Objectify.class);

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
		ObjectifyService.init(objectifyFactoryManufacturer.createObjectifyFactory());
		ObjectifyService.register(Domain.class);
		ObjectifyService.register(GoogleSignin.class);
		ObjectifyService.register(User.class);
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