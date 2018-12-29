package net.officefloor.app.subscription.store;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide {@link Objectify}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/*
	 * ================= ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(Objectify.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {
		ObjectifyService.init();
		ObjectifyService.register(Domain.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ObjectifyManagedObject(ObjectifyService.ofy());
	}

	/**
	 * {@link ManagedObject} for {@link Objectify}.
	 */
	private static class ObjectifyManagedObject implements ManagedObject {

		private final Objectify objectify;

		private ObjectifyManagedObject(Objectify objectify) {
			this.objectify = objectify;
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