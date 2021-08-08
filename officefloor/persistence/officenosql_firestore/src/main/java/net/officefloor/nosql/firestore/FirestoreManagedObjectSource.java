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

		// Only load if not typing
		if (mosContext.isLoadingType()) {
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
