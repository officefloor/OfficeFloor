/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.compile.integrate.office;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.spi.office.EscalationExplorer;
import net.officefloor.compile.spi.office.EscalationExplorerContext;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link EscalationExplorer} exploring the execution trees of each
 * {@link OfficeEscalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationExplorerTest extends OfficeFrameTestCase {

	/**
	 * {@link CompileOfficeFloor}.
	 */
	private final CompileOfficeFloor compile = new CompileOfficeFloor();

	/**
	 * Ensure can explore {@link OfficeEscalation}.
	 */
	public void testSingleEscalation() throws Exception {
		this.doExplore(V(Exception.class, HandleSection.class, (explore) -> {
			assertEquals("Incorrect initial method", getManagedFunctionName("service"),
					explore.getInitialManagedFunction().getManagedFunctionName());
		}));
	}

	public static class HandleSection {
		public void service() {
			// no operation
		}
	}

	/**
	 * Ensure can explore multiple {@link OfficeEscalation} instances.
	 */
	public void testMultipleEscalations() throws Exception {
		this.doExplore(V(IOException.class, HandleSection.class, (explore) -> {
			assertEquals("Incorrect initial method", "HANDLE_" + IOException.class.getName() + ".service",
					explore.getInitialManagedFunction().getManagedFunctionName());
		}), V(SQLException.class, HandleSection.class, (explore) -> {
			assertEquals("Incorrect initial method", "HANDLE_" + SQLException.class.getName() + ".service",
					explore.getInitialManagedFunction().getManagedFunctionName());
		}));
	}

	/**
	 * Ensure can explore next {@link ManagedFunction}.
	 */
	public void testNextFunction() throws Exception {
		this.doExplore(V(Exception.class, NextFunction.class, (context) -> {
			ExecutionManagedFunction initiate = context.getInitialManagedFunction();
			assertEquals("Incorrect initial function", getManagedFunctionName("service"),
					initiate.getManagedFunctionName());
			ExecutionManagedFunction next = initiate.getNextManagedFunction();
			assertEquals("Incorrect next function", getManagedFunctionName("next"), next.getManagedFunctionName());
		}));
	}

	public static class NextFunction {
		@Next("next")
		public void service() {
			// no operation
		}

		public void next() {
			// no operation
		}
	}

	/**
	 * Ensure can explore a {@link Flow} to another {@link ManagedFunction}.
	 */
	public void testFlowToFunction() throws Exception {
		this.doExplore(V(Exception.class, FlowToFunction.class, (context) -> {
			ExecutionManagedFunction initiate = context.getInitialManagedFunction();
			assertEquals("Incorrect function", getManagedFunctionName("service"), initiate.getManagedFunctionName());
			ManagedFunctionFlowType<?>[] flows = initiate.getManagedFunctionType().getFlowTypes();
			assertEquals("Incorrect number of flows", 1, flows.length);
			ExecutionManagedFunction function = initiate.getManagedFunction(flows[0]);
			assertEquals("Incorrect function", getManagedFunctionName("function"), function.getManagedFunctionName());
		}));
	}

	@FlowInterface
	public static interface FunctionFlows {
		void function();
	}

	public static class FlowToFunction {
		public void service(FunctionFlows flows) {
			// no operation
		}

		public void function() {
			// no operation
		}
	}

	/**
	 * Ensure can explore an {@link Escalation} to its handling
	 * {@link ManagedFunction}.
	 */
	public void testEscalationToFunction() throws Exception {
		this.doExplore(V(Exception.class, EscalationToFunction.class, (context) -> {
			ExecutionManagedFunction trigger = context.getInitialManagedFunction();
			assertEquals("Incorrect function", getManagedFunctionName("service"), trigger.getManagedFunctionName());
			ManagedFunctionEscalationType[] escalations = trigger.getManagedFunctionType().getEscalationTypes();
			assertEquals("Incorrect number of escalations", 1, escalations.length);
			ExecutionManagedFunction handle = trigger.getManagedFunction(escalations[0]);
			assertEquals("Incorrect function", getManagedFunctionName("handle"), handle.getManagedFunctionName());
		}));
	}

	public static class EscalationToFunction {
		public void service() throws SQLException {
			// no operation
		}

		public void handle(@Parameter SQLException ex) {
			// no operation
		}
	}

	/**
	 * Ensure can dynamically obtain the {@link ExecutionManagedFunction}.
	 */
	public void testDynamicFunction() throws Exception {
		this.doExplore(V(Exception.class, DynamicFunction.class, (context) -> {
			// Ensure can dynamically obtain function
			ExecutionManagedFunction dynamic = context.getManagedFunction(getManagedFunctionName("dynamic"));
			assertNotNull("Should find dynamic", dynamic);
			assertEquals("Incorrect dynamic", getManagedFunctionName("dynamic"), dynamic.getManagedFunctionName());
		}));
	}

	public static class DynamicFunction {
		public void service() {
		}

		public void dynamic() {
		}
	}

	/**
	 * Obtains the {@link ManagedFunction} name.
	 * 
	 * @param methodName Name of method.
	 * @return {@link ManagedFunction} name.
	 */
	private static String getManagedFunctionName(String methodName) {
		return "HANDLE_" + Exception.class.getName() + "." + methodName;
	}

	/**
	 * Short hand convenience constructor.
	 * 
	 * @param escalationClass {@link Escalation} {@link Class}.
	 * @param sectionClass    {@link OfficeSection} {@link Class}.
	 * @param validator       Validates the explore.
	 * @return {@link EscalationVerify}.
	 */
	private static EscalationVerify V(Class<? extends Throwable> escalationClass, Class<?> sectionClass,
			Consumer<EscalationExplorerContext> validator) {
		return new EscalationVerify(escalationClass, sectionClass, validator);
	}

	/**
	 * Undertakes the {@link EscalationExplorer} test.
	 * 
	 * @param verifications {@link EscalationVerify} to verify exploration.
	 */
	private void doExplore(EscalationVerify... verifications) throws Exception {
		Map<String, EscalationExplorerContext> explored = new HashMap<>();
		this.compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Configure escalations
			for (EscalationVerify verify : verifications) {
				OfficeSection section = context.addSection("HANDLE_" + verify.escalationClass.getName(),
						verify.sectionClass);
				OfficeEscalation escalation = office.addOfficeEscalation(verify.escalationClass.getName());
				office.link(escalation, section.getOfficeSectionInput("service"));
			}

			// Explore escalation
			office.addOfficeEscalationExplorer((explore) -> {
				String escalationType = explore.getOfficeEscalationType();

				// Undertake verification of exploring
				for (EscalationVerify verify : verifications) {
					if (verify.escalationClass.getName().equals(escalationType)) {
						verify.validator.accept(explore);
					}
				}

				// Register explored
				explored.put(escalationType, explore);
			});
		});
		this.compile.compileAndOpenOfficeFloor();

		// Ensure each escalation explored
		for (EscalationVerify verify : verifications) {
			String escalationType = verify.escalationClass.getName();
			assertNotNull("No exploration of escalation " + escalationType, explored.get(escalationType));
		}
		assertEquals("Incorrect number of explorations", verifications.length, explored.size());
	}

	private static class EscalationVerify {

		private final Class<? extends Throwable> escalationClass;

		private final Class<?> sectionClass;

		private final Consumer<EscalationExplorerContext> validator;

		private EscalationVerify(Class<? extends Throwable> escalationClass, Class<?> sectionClass,
				Consumer<EscalationExplorerContext> validator) {
			this.escalationClass = escalationClass;
			this.sectionClass = sectionClass;
			this.validator = validator;
		}
	}

}
