/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.comet.spi;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

/**
 * {@link CometRequestServicer} {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometRequestServicerManagedObjectSource
		extends
		AbstractManagedObjectSource<CometRequestServicerManagedObjectSource.Dependencies, None> {

	/**
	 * {@link CometRequestServicer} dependency keys.
	 */
	public static enum Dependencies {
		SERVER_GWT_RPC_CONNECTION, COMET_SERVICE
	}

	/*
	 * ========================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {

		// Configure meta-data
		context.setObjectClass(CometRequestServicer.class);
		context.setManagedObjectClass(CometRequestServicerManagedObject.class);
		context.addDependency(Dependencies.SERVER_GWT_RPC_CONNECTION,
				ServerGwtRpcConnection.class);
		context.addDependency(Dependencies.COMET_SERVICE, CometService.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new CometRequestServicerManagedObject();
	}

	/**
	 * {@link CometRequestServicer} {@link ManagedObject}.
	 */
	private static class CometRequestServicerManagedObject implements
			CoordinatingManagedObject<Dependencies>, CometRequestServicer {

		/**
		 * {@link ServerGwtRpcConnection}.
		 */
		private ServerGwtRpcConnection<CometResponse> connection;

		/**
		 * {@link CometService}.
		 */
		private CometService cometService;

		/*
		 * ===================== ManagedObject ===========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {

			// Obtain dependencies
			this.connection = (ServerGwtRpcConnection<CometResponse>) registry
					.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION);
			this.cometService = (CometService) registry
					.getObject(Dependencies.COMET_SERVICE);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== CometRequestServicer ====================
		 */

		@Override
		public void service() {
			this.cometService.service(this.connection);
		}
	}

}