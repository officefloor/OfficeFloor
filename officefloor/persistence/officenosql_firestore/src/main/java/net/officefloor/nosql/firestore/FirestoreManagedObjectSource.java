package net.officefloor.nosql.firestore;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.cloud.firestore.Firestore;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * {@link Firestore}.
	 */
	private Firestore firestore;

	/*
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the logger
		this.logger = mosContext.getLogger();

		// Load the meta-data
		context.setObjectClass(Firestore.class);

		// Supplier setup
		if (this.firestore != null) {
			return;
		}

		// Obtain firestore
		this.firestore = FirestoreConnect.connect(mosContext);
	}

	@Override
	public void stop() {

		// Ensure close connection on stopping
		if (this.firestore != null) {
			try {
				this.firestore.close();
			} catch (Exception ex) {
				this.logger.log(Level.WARNING, "Failed to close Firestore", ex);
			}
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================= ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.firestore;
	}

}
