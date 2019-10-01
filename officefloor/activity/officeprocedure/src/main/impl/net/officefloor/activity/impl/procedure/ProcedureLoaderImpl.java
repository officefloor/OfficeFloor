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
package net.officefloor.activity.impl.procedure;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureService;
import net.officefloor.compile.impl.util.CompileUtil;

/**
 * {@link ProcedureLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderImpl implements ProcedureLoader {

	/**
	 * Provides the {@link ProcedureService} instances.
	 */
	private final Supplier<Iterable<ProcedureService>> loadServices;

	/**
	 * Instantiate.
	 * 
	 * @param loadServices Provides the {@link ProcedureService} instances.
	 */
	public ProcedureLoaderImpl(Supplier<Iterable<ProcedureService>> loadServices) {
		this.loadServices = loadServices;
	}

	/*
	 * ========================== ProcedureLoader ==========================
	 */

	@Override
	public Procedure[] listProcedures(String className) throws Exception {

		// Collect the listing of procedures
		List<Procedure> procedures = new LinkedList<Procedure>();
		for (ProcedureService service : this.loadServices.get()) {

			// Obtain the procedure names
			String[] procedureNames = service.listProcedures(className);
			if (procedureNames != null) {
				for (String procedureName : procedureNames) {
					if (!CompileUtil.isBlank(procedureName)) {
						procedures.add(new ProcedureImpl(procedureName.trim(), service.getServiceName()));
					}
				}
			}
		}

		// Return the procedures
		return procedures.toArray(new Procedure[procedures.size()]);
	}

	/**
	 * {@link Procedure} implementation.
	 */
	private static class ProcedureImpl implements Procedure {

		/**
		 * Procedure name.
		 */
		private final String procedureName;

		/**
		 * {@link ProcedureService} name.
		 */
		private final String serviceName;

		/**
		 * Instantiate.
		 * 
		 * @param procedureName Procedure name.
		 * @param serviceName   {@link ProcedureService} name.
		 */
		public ProcedureImpl(String procedureName, String serviceName) {
			this.procedureName = procedureName;
			this.serviceName = serviceName;
		}

		/*
		 * =================== Procedure ======================
		 */

		@Override
		public String getProcedureName() {
			return this.procedureName;
		}

		@Override
		public String getServiceName() {
			return this.serviceName;
		}
	}

}