/*-
 * #%L
 * Firestore
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
