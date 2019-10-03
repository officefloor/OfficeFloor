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

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.ProcedureService;
import net.officefloor.activity.procedure.ProcedureServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;

/**
 * {@link ProcedureLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderImpl implements ProcedureLoader {

	/**
	 * {@link Loader}.
	 */
	private final Loader loader;

	/**
	 * Instantiate.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 */
	public ProcedureLoaderImpl(OfficeFloorCompiler compiler) {
		this.loader = new Loader() {

			@Override
			public Iterable<ProcedureService> loadServices() {
				return compiler.createRootSourceContext().loadOptionalServices(ProcedureServiceFactory.class);
			}

			@Override
			public ManagedFunctionLoader getManagedFunctionLoader() {
				return compiler.getManagedFunctionLoader();
			}

			@Override
			public PropertyList createPropertyList() {
				return compiler.createPropertyList();
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return compiler.getCompilerIssues().addIssue(compiler, issueDescription, cause);
			}
		};
	}

	/*
	 * ========================== ProcedureLoader ==========================
	 */

	@Override
	public Procedure[] listProcedures(Class<?> clazz) {

		// Collect the listing of procedures
		List<Procedure> procedures = new LinkedList<Procedure>();
		for (ProcedureService service : this.loader.loadServices()) {

			// Obtain the service name
			String serviceName = service.getServiceName();

			// Attempt to list the procedures
			String[] procedureNames;
			try {
				procedureNames = service.listProcedures(clazz);
			} catch (Exception ex) {
				this.loader.addIssue("Failed to list procedures from service " + serviceName + " ["
						+ service.getClass().getName() + "]", ex);
				procedureNames = null; // No procedures
			}

			// Obtain the procedure names
			if (procedureNames != null) {
				for (String procedureName : procedureNames) {
					if (!CompileUtil.isBlank(procedureName)) {
						procedures.add(new ProcedureImpl(procedureName.trim(), serviceName));
					}
				}
			}
		}

		// Return the procedures
		return procedures.toArray(new Procedure[procedures.size()]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ManagedFunctionType<Indexed, Indexed> loadProcedureType(Class<?> clazz, String procedureName,
			String serviceName) {

		// Obtain the managed function loader
		ManagedFunctionLoader loader = this.loader.getManagedFunctionLoader();

		// Load the managed function type
		PropertyList properties = this.loader.createPropertyList();
		properties.addProperty(ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME).setValue(clazz.getName());
		properties.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME).setValue(serviceName);
		properties.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME).setValue(procedureName);
		FunctionNamespaceType namespace = loader.loadManagedFunctionType(ProcedureManagedFunctionSource.class,
				properties);

		// Ensure have namespace
		if (namespace == null) {
			return null; // failed to load
		}

		// Return the managed function type (should always be just one)
		return (ManagedFunctionType<Indexed, Indexed>) namespace.getManagedFunctionTypes()[0];
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

	/**
	 * Loader.
	 */
	private static interface Loader {

		/**
		 * Loads the {@link ProcedureService} instances.
		 * 
		 * @return {@link ProcedureService} instances.
		 */
		Iterable<ProcedureService> loadServices();

		/**
		 * Obtains the {@link ManagedFunctionLoader}.
		 * 
		 * @return {@link ManagedFunctionLoader}.
		 */
		ManagedFunctionLoader getManagedFunctionLoader();

		/**
		 * Creates a {@link PropertyList}.
		 * 
		 * @return {@link PropertyList}.
		 */
		PropertyList createPropertyList();

		/**
		 * Adds a {@link CompilerIssue}.
		 * 
		 * @param issueDescription Description of the issue.
		 * @param cause            Cause.
		 * @return {@link CompileError}.
		 */
		CompileError addIssue(String issueDescription, Throwable cause);
	}

}