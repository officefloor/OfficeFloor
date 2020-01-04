/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Validates passing a parameter between two {@link ManagedFunction} instances
 * of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class PassParameterTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates with any {@link Team}.
	 */
	public void test_any_PassParameterBetweenFunctions() throws Exception {
		this.doPassParameterBetweenFunctionsTest(null);
	}

	/**
	 * Validates with {@link PassiveTeamSource}.
	 */
	public void test_passive_PassParameterBetweenFunctions() throws Exception {
		this.doPassParameterBetweenFunctionsTest(PassiveTeamSource.createPassiveTeam());
	}

	/**
	 * Validates with {@link OnePersonTeamSource}.
	 */
	public void test_onePerson_PassParameterBetweenFunctions() throws Exception {
		this.doPassParameterBetweenFunctionsTest(OnePersonTeamSource.createOnePersonTeam("TEAM"));
	}

	/**
	 * Validates with {@link ExecutorCachedTeamSource}.
	 */
	public void test_executor_PassParameterBetweenFunctions() throws Exception {
		this.doPassParameterBetweenFunctionsTest(new ExecutorCachedTeamSource().createTeam(0));
	}

	/**
	 * Validates passing a parameter between two {@link ManagedFunction} instances
	 * of an {@link Office}.
	 * 
	 * @param team {@link Team}.
	 */
	private void doPassParameterBetweenFunctionsTest(Team team) throws Exception {

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the team
		if (team != null) {
			this.constructTeam("TEAM", team);
		}

		// Add the first function
		FunctionOne functionOne = new FunctionOne(parameter);
		ManagedFunctionBuilder<None, FunctionOneDelegatesEnum> functionOneBuilder = this.constructFunction("SENDER",
				functionOne);
		if (team != null) {
			functionOneBuilder.setResponsibleTeam("TEAM");
		}
		functionOneBuilder.linkFlow(FunctionOneDelegatesEnum.FUNCTION_TWO.ordinal(), "RECEIVER", Object.class, false);

		// Add the second function
		FunctionTwo functionTwo = new FunctionTwo();
		ManagedFunctionBuilder<FunctionTwoDependenciesEnum, None> functionTwoBuilder = this
				.constructFunction("RECEIVER", functionTwo);
		if (team != null) {
			functionTwoBuilder.setResponsibleTeam("TEAM");
		}
		functionTwoBuilder.linkParameter(FunctionTwoDependenciesEnum.PARAMETER, Object.class);

		// Invoke first function
		this.invokeFunction("SENDER", null);

		// Validate the parameter was passed
		assertEquals("Incorrect parameter", parameter, functionTwo.getParameter());
	}

	/**
	 * First {@link ManagedFunction} type for testing.
	 */
	private class FunctionOne implements ManagedFunction<None, FunctionOneDelegatesEnum> {

		/**
		 * Parameter to invoke delegate with.
		 */
		protected final Object parameter;

		/**
		 * Initiate.
		 * 
		 * @param parameter Parameter to invoke delegate with.
		 */
		public FunctionOne(Object parameter) {
			this.parameter = parameter;
		}

		/*
		 * ==================== ManaagedFunction ====================
		 */

		@Override
		public void execute(ManagedFunctionContext<None, FunctionOneDelegatesEnum> context) throws Exception {

			// Delegate to the next function
			context.doFlow(FunctionOneDelegatesEnum.FUNCTION_TWO, this.parameter, null);
		}
	}

	private enum FunctionOneDelegatesEnum {
		FUNCTION_TWO
	}

	/**
	 * Second {@link ManagedFunction} type for testing.
	 */
	private class FunctionTwo implements ManagedFunction<FunctionTwoDependenciesEnum, None> {

		/**
		 * Parameter received when invoked.
		 */
		protected volatile Object parameter;

		/**
		 * Obtains the received parameter;
		 * 
		 * @return Received parameter;
		 */
		public Object getParameter() {
			return this.parameter;
		}

		/*
		 * ==================== ManagedFunction ====================
		 */

		@Override
		public void execute(ManagedFunctionContext<FunctionTwoDependenciesEnum, None> context) throws Exception {

			// Store the parameter
			this.parameter = context.getObject(FunctionTwoDependenciesEnum.PARAMETER);
		}
	}

	private enum FunctionTwoDependenciesEnum {
		PARAMETER
	}

}
