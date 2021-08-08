/*-
 * #%L
 * Firestore
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
