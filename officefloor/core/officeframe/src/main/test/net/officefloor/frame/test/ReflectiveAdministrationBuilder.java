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
package net.officefloor.frame.test;

import java.awt.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.FlowCallback;

/**
 * Reflective {@link AdministrationBuilder}.
 *
 * @author Daniel Sagenschneider
 */
public class ReflectiveAdministrationBuilder extends AbstractAdministratorSource<Object, Indexed>
		implements Administration<Object, Indexed> {

	/**
	 * {@link AbstractOfficeConstructTestCase}.
	 */
	private final AbstractOfficeConstructTestCase testCase;

	/**
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder;

	/**
	 * {@link Class} of the object.
	 */
	private final Class<?> clazz;

	/**
	 * Object should the {@link Method} for the {@link AdministrationDuty} not be static.
	 */
	private final Object object;

	/**
	 * {@link AdministrationBuilder} for this
	 * {@link ReflectiveAdministrationBuilder}.
	 */
	private final AdministrationBuilder<Indexed> administratorBuilder;

	/**
	 * {@link Map} of {@link AdministrationDuty} key to {@link ReflectiveDutyBuilder}.
	 */
	private final Map<Integer, ReflectiveDutyBuilder> duties = new HashMap<>();

	/**
	 * Index of the next {@link AdministrationDuty}.
	 */
	private int nextDutyIndex = 0;

	/**
	 * Instantiate.
	 * 
	 * @param clazz
	 *            {@link Class} to determine the {@link Method} instances for
	 *            the {@link AdministrationDuty} instances.
	 * @param object
	 *            Optional {@link Object} for non-static methods.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	public <C> ReflectiveAdministrationBuilder(Class<C> clazz, C object, OfficeBuilder officeBuilder,
			AbstractOfficeConstructTestCase testCase) {
		this.clazz = clazz;
		this.object = object;
		this.officeBuilder = officeBuilder;
		this.testCase = testCase;

		// Construct this administrator
		this.administratorBuilder = this.officeBuilder.addAdministrator(this.clazz.getSimpleName(), this.getClass());
	}

	/**
	 * Constructs the {@link AdministrationDuty}.
	 * 
	 * @param methodName
	 *            Name of the {@link Method} for the {@link AdministrationDuty}.
	 * @return {@link ReflectiveDutyBuilder} to build the {@link AdministrationDuty}.
	 */
	public ReflectiveDutyBuilder constructDuty(String methodName) {

		// Obtain the method
		Method functionMethod = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				functionMethod = method;
			}
		}
		if (functionMethod == null) {
			TestCase.fail("No method '" + methodName + "' on class " + clazz.getName());
		}

		// Obtain next index
		int dutyIndex = this.nextDutyIndex++;

		// Create the duty builder
		ReflectiveDutyBuilder duty = new ReflectiveDutyBuilder(functionMethod);

		// Register the duty
		this.duties.put(dutyIndex, duty);

		// Return the duty
		return duty;
	}

	/**
	 * =========================== Administrator =======================
	 */

	@Override
	public AdministrationDuty<Object, ?, ?> getDuty(DutyKey<Indexed> dutyKey) {
		return this.duties.get(dutyKey.getIndex());
	}

	/**
	 * ======================== AdministratorSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Object, Indexed> context) throws Exception {

		// Load the meta data
		int dutyIndex = 0;
		ReflectiveDutyBuilder duty = this.duties.get(dutyIndex++);
		while (duty != null) {
			
			DutyMetaDataContext builder = context.addDuty(duty.method.getName());
			
			// Obtain next duty
			duty = this.duties.get(dutyIndex++);
		}
		
	}

	@Override
	public Administration<Object, Indexed> createAdministrator() throws Throwable {
		return this;
	}

	/**
	 * Reflective builder for a {@link AdministrationDuty} as a {@link Method}.
	 */
	public class ReflectiveDutyBuilder implements AdministrationDuty<Object, Indexed, Indexed> {

		/**
		 * {@link Method} for this {@link AdministrationDuty}.
		 */
		private final Method method;

		/**
		 * {@link DutyBuilder} for this {@link ReflectiveDutyBuilder}.
		 */
		private final DutyBuilder dutyBuilder;

		/**
		 * Types for the parameters of the {@link Method}.
		 */
		private final Class<?>[] parameterTypes;

		/**
		 * {@link ParameterFactory} instances for the parameters of the
		 * {@link Method}.
		 */
		private final ParameterFactory[] parameterFactories;

		/**
		 * Instantiate.
		 * 
		 * @param method
		 *            {@link Method} for this {@link AdministrationDuty}.
		 */
		public ReflectiveDutyBuilder(Method method) {
			this.method = method;

			// Construct the duty
			this.dutyBuilder = ReflectiveAdministrationBuilder.this.administratorBuilder.addDuty(this.method.getName());

			// Obtain the parameter types
			this.parameterTypes = this.method.getParameterTypes();

			// Ensure first parameter is always the extensions
			Assert.assertTrue("Duty method " + this.method.getName() + " must have at least one parameter (extensions)",
					this.parameterTypes.length >= 1);
			Assert.assertEquals("First parameter for method " + this.method.getName() + " must be a List (extensions)",
					List.class, this.parameterTypes[0]);

			// Create the parameter factories
			this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
			this.parameterFactories[0] = new ExtensionsParameterFactory();
		}

		public void buildFlow(String functionName) {

		}

		/*
		 * ======================== Duty ==================================
		 */

		@Override
		public void doDuty(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

			// Obtain the argument values
			Object[] arguments = new Object[this.parameterTypes.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = this.parameterFactories[i].createParamater(context);
			}

			// Invoke the method on object for duty logic
			try {
				this.method.invoke(ReflectiveAdministrationBuilder.this.object, arguments);
			} catch (InvocationTargetException ex) {
				// Throw cause of exception
				throw ex.getCause();
			}
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link AdministrationContext}.
	 */
	private interface ParameterFactory {
		Object createParamater(AdministrationContext<Object, Indexed, Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to obtain the extenion interfaces.
	 */
	private class ExtensionsParameterFactory implements ParameterFactory {
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			return context.getExtensionInterfaces();
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the flow.
	 */
	private class ReflectiveFlowParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index
		 * 
		 */
		public ReflectiveFlowParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public void doFlow(Object parameter, FlowCallback callback) {
					context.doFlow(ReflectiveFlowParameterFactory.this.index, parameter, callback);
				}
			};
		}
	}

}