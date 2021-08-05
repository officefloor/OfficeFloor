package net.officefloor.nosql.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;

/**
 * {@link SupplierSource} for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSupplierSource extends AbstractSupplierSource {

	/*
	 * ====================== SupplierSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Provide Firestore managed object
		context.addManagedObjectSource(null, Firestore.class, new FirestoreManagedObjectSource());
	}

	@Override
	public void terminate() {
		// Managed Object will shutdown Firestore
	}

}
