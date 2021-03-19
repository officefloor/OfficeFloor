/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.plugin.variable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentor;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentorContext;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeExtensionService} for {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * Context logic.
	 */
	@FunctionalInterface
	public static interface ContextLogic<R, T extends Throwable> {

		/**
		 * Logic.
		 * 
		 * @return Result.
		 * @throws T Possible failure.
		 */
		R run() throws T;
	}

	/**
	 * {@link ThreadLocal} decorators for variables.
	 */
	private static final ThreadLocal<Map<String, Consumer<Var<?>>>> threadLocalDecorators = new ThreadLocal<>();

	/**
	 * Runs within context.
	 * 
	 * @param decorators {@link Map} of decorators by variable name.
	 * @param logic      {@link ContextLogic}.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R runInContext(Map<String, Consumer<Var<?>>> decorators,
			ContextLogic<R, T> logic) throws T {
		threadLocalDecorators.set(decorators);
		try {
			return logic.run();
		} finally {
			threadLocalDecorators.remove();
		}
	}

	/*
	 * ======================== OfficeExtensionService ==========================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Augment for the variable
		officeArchitect.addManagedFunctionAugmentor(new VariableManagedFunctionAugmentor(officeArchitect));
	}

	/**
	 * {@link ManagedFunctionAugmentor} for the {@link Var}.
	 */
	private static class VariableManagedFunctionAugmentor implements ManagedFunctionAugmentor {

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect office;

		/**
		 * {@link Var} fields for the application.
		 */
		private final Map<String, OfficeManagedObject> variables = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param office {@link OfficeArchitect}.
		 */
		private VariableManagedFunctionAugmentor(OfficeArchitect office) {
			this.office = office;
		}

		/*
		 * ===================== ManagedFunctionAugmentor =======================
		 */

		@Override
		public void augmentManagedFunction(ManagedFunctionAugmentorContext context) {

			// Obtain the variable decorators
			Map<String, Consumer<Var<?>>> decorators = threadLocalDecorators.get();

			// Load any variables
			ManagedFunctionType<?, ?> functionType = context.getManagedFunctionType();
			NEXT_OBJECT: for (ManagedFunctionObjectType<?> objectType : functionType.getObjectTypes()) {

				// Determine if variable
				String variableName = VariableAnnotation.extractPossibleVariableName(objectType);
				if (variableName == null) {
					continue NEXT_OBJECT; // not variable
				}

				// Obtain the object
				AugmentedFunctionObject parameter = context.getFunctionObject(objectType.getObjectName());

				// Include the variable
				OfficeManagedObject variable = this.variables.get(variableName);
				if (variable == null) {

					// Obtain the possible decorator
					Consumer<Var<?>> decorator = (decorators == null) ? null : decorators.get(variableName);

					// Create the variable source
					@SuppressWarnings({ "unchecked", "rawtypes" })
					VariableManagedObjectSource<?> mos = new VariableManagedObjectSource(decorator);

					// Create the variable
					String moVariableName = VariableManagedObjectSource.VARIABLE_NAME_PREFIX + variableName;
					variable = this.office.addOfficeManagedObjectSource(moVariableName, mos)
							.addOfficeManagedObject(variableName, ManagedObjectScope.THREAD);

					// Register the variable (for expected re-use)
					this.variables.put(variableName, variable);
				}

				// Link variable
				context.link(parameter, variable);
			}
		}
	}

}
