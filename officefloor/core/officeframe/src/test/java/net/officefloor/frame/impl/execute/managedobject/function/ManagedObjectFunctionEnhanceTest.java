/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedobject.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure can enhance {@link ManagedFunction} provided by
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ManagedObjectFunctionEnhanceTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure able to enhance {@link ManagedFunction} provided by
	 * {@link ManagedObjectSource}.
	 */
	@Test
	public void enhanceFunction() throws Exception {

		// Build the managed object
		EnhanceFunctionManagedObjectSource mos = new EnhanceFunctionManagedObjectSource();
		ManagedObjectBuilder<FlowKeys> builder = this.construct.constructManagedObject("MOS", mos, null);

		// Enhance managed function
		builder.addFunctionEnhancer((context) -> {
			assertEquals("of-MOS.FUNCTION", context.getFunctionName(), "Incorrect function name");
			assertSame(mos, context.getManagedFunctionFactory(), "Incorrect function factory");
			assertTrue(context.isUsingManagedObject(), "Should be using the managed object");
			assertEquals("TEAM", context.getResponsibleTeam(), "Incorrect team");
			ManagedObjectFunctionDependency[] functionDependencies = context.getFunctionDependencies();
			assertEquals(1, functionDependencies.length, "Incorrect number of function dependencies");
			assertEquals("DEPENDENCY", functionDependencies[0].getFunctionDependencyName(),
					"Incorrect function dependency");

			// Change the responsible team
			context.setResponsibleTeam("RESPONSIBLE");
		});

		// Provide the function dependency
		String officeName = this.construct.getOfficeName();
		this.construct.constructManagedObject("MOCK", "FUNCTION_DEPENDENCY", officeName);
		ManagingOfficeBuilder<FlowKeys> managingOffice = builder.setManagingOffice(officeName);
		managingOffice.setInputManagedObjectName("of-MOS");
		managingOffice.mapFunctionDependency("DEPENDENCY", "FUNCTION_DEPENDENCY");

		// Provide team for enhanced
		final List<Job> jobs = new LinkedList<>();
		this.construct.constructTeam("of-MOS.RESPONSIBLE", new Team() {

			@Override
			public void startWorking() {
				// Nothing to start
			}

			@Override
			public void assignJob(Job job) throws TeamOverloadException, Exception {
				jobs.add(job);
			}

			@Override
			public void stopWorking() {
				// Nothing to stop
			}
		});

		// Use the managed object
		ReflectiveFunctionBuilder function = this.construct.constructFunction(new TestFunction(), "function");
		function.buildObject("MOS", ManagedObjectScope.THREAD);
		function.buildObject("FUNCTION_DEPENDENCY", ManagedObjectScope.THREAD);

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.construct.constructOfficeFloor();
		assertEquals(0, jobs.size(), "Should not trigger on compile");

		// Open OfficeFloor, which should trigger function
		officeFloor.openOfficeFloor();
		assertEquals(1, jobs.size(), "Should register job on open");
		assertFalse(mos.isManagedFunctionExecuted, "Should be assigned to team, so not yet executed");

		// Run job (to execute function)
		jobs.get(0).run();
		assertTrue(mos.isManagedFunctionExecuted, "Should now have function executed by team");
	}

	public static class TestFunction {
		public void function(EnhanceFunctionManagedObjectSource dependency, String functionDependency) {
			// test method
		}
	}

	public static enum FlowKeys {
		FLOW
	}

	public static enum DependencyKeys {
		MANAGED_OBJECT_DEPENDENCY, FUNCTION_DEPENDENCY
	}

	/**
	 * {@link ManagedObjectSource} to test enhancing providing
	 * {@link ManagedFunction}.
	 */
	@TestSource
	public static class EnhanceFunctionManagedObjectSource extends AbstractManagedObjectSource<None, FlowKeys>
			implements ManagedObject, ManagedFunctionFactory<DependencyKeys, None>,
			ManagedFunction<DependencyKeys, None> {

		/**
		 * Indicates whether the {@link ManagedFunction} has been executed.
		 */
		private boolean isManagedFunctionExecuted = false;

		/*
		 * ====================== ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, FlowKeys> context) throws Exception {
			ManagedObjectSourceContext<FlowKeys> mosContext = context.getManagedObjectSourceContext();

			// Provide meta-data
			context.setObjectClass(this.getClass());
			context.addFlow(FlowKeys.FLOW, null);
			mosContext.getFlow(FlowKeys.FLOW).linkFunction("FUNCTION");

			// Provide the managed function
			ManagedObjectFunctionBuilder<DependencyKeys, None> function = mosContext.addManagedFunction("FUNCTION",
					this);

			// Link the managed object
			function.linkManagedObject(DependencyKeys.MANAGED_OBJECT_DEPENDENCY);

			// Provide function dependency
			ManagedObjectFunctionDependency dependency = mosContext.addFunctionDependency("DEPENDENCY", String.class);
			function.linkObject(DependencyKeys.FUNCTION_DEPENDENCY, dependency);

			// Specify team
			function.setResponsibleTeam("TEAM");

			// Run the function on start up
			mosContext.addStartupFunction("FUNCTION", null);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ========================= ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ManagedFunction =========================
		 */

		@Override
		public ManagedFunction<DependencyKeys, None> createManagedFunction() throws Throwable {
			return this;
		}

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {
			this.isManagedFunctionExecuted = true;
		}
	}

}
