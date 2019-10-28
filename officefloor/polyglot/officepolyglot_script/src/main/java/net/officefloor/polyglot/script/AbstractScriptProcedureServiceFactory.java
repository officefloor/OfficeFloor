/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.script;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureService;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureServiceContext;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link ProcedureServiceFactory} providing abstract support for Scripts.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractScriptProcedureServiceFactory implements ProcedureServiceFactory {

	/*
	 * ==================== ProcedureServiceFactory ===================
	 */

	@Override
	public ProcedureService createService(ServiceContext context) throws Throwable {
		// TODO implement ServiceFactory<ProcedureService>.createService
		throw new UnsupportedOperationException("TODO implement ServiceFactory<ProcedureService>.createService");
	}

	/**
	 * {@link ProcedureService} providing abstract support for Scripts.
	 */
	private static class AbstractScriptProcedureService implements ManagedFunctionProcedureService {

		@Override
		public String getServiceName() {
			// TODO implement ProcedureService.getServiceName
			throw new UnsupportedOperationException("TODO implement ProcedureService.getServiceName");
		}

		@Override
		public String[] listProcedures(String resource) throws Exception {
			// TODO implement ProcedureService.listProcedures
			throw new UnsupportedOperationException("TODO implement ProcedureService.listProcedures");
		}

		@Override
		public void loadManagedFunction(ManagedFunctionProcedureServiceContext context) throws Exception {
			// TODO implement ManagedFunctionProcedureService.loadManagedFunction
			throw new UnsupportedOperationException(
					"TODO implement ManagedFunctionProcedureService.loadManagedFunction");
		}
	}

}