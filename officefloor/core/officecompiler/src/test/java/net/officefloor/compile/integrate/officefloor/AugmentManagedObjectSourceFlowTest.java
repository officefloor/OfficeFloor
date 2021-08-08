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

package net.officefloor.compile.integrate.officefloor;

import java.util.function.BiConsumer;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to augment {@link ManagedObjectSource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentManagedObjectSourceFlowTest extends OfficeFrameTestCase {

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceFlowAlreadyLinked() throws Exception {
		this.doAugmentedManagedObjectSourceTest("MOS", true, null);
	}

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceFlow() throws Exception {
		this.doAugmentedManagedObjectSourceTest("MOS", false, (compile, mos) -> {
			compile.officeFloor((context) -> this.createOfficeFloorManagedObjectSource(context, mos));
		});
	}

	private OfficeFloorManagedObjectSource createOfficeFloorManagedObjectSource(CompileOfficeFloorContext context,
			AugmentFlowManagedObjectSource mos) {
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorManagedObjectSource source = deployer.addManagedObjectSource("MOS", mos);
		deployer.link(source.getManagingOffice(), context.getDeployedOffice());
		OfficeFloorInputManagedObject inputMo = deployer.addInputManagedObject("MOS",
				AugmentFlowManagedObjectSource.class.getName());
		deployer.link(source, inputMo);
		return source;
	}

	/**
	 * Ensure can augment the {@link OfficeManagedObjectSource}.
	 */
	public void testAugmentOfficeManagedObjectSourceFlow() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.MOS", false, (compile, mos) -> {
			compile.office((context) -> {
				context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Ensure can augment the {@link SectionManagedObjectSource}.
	 */
	public void testAugmentSectionManagedObjectSourceFlow() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.SECTION.MOS", false, (compile, mos) -> {
			compile.section((context) -> {
				context.getSectionDesigner().addSectionManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Undertakes the augment {@link ManagedObjectSource} test.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param isAlreadyLinked         Indicates if should already be linked.
	 * @param loader                  Loads the {@link ManagedObjectSource}.
	 */
	private void doAugmentedManagedObjectSourceTest(String managedObjectSourceName, boolean isAlreadyLinked,
			BiConsumer<CompileOfficeFloor, AugmentFlowManagedObjectSource> loader) throws Exception {

		// Create the augment
		AugmentFlowManagedObjectSource mos = new AugmentFlowManagedObjectSource();

		// Compile with the augmented managed object source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		Closure<Boolean> isSectionObjectAvailable = new Closure<>(false);
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Obtain the section input
			DeployedOfficeInput input = context.getDeployedOffice().getDeployedOfficeInput("SERVICE", "service");

			// Determine if already linked
			if (isAlreadyLinked) {
				OfficeFloorManagedObjectSource source = this.createOfficeFloorManagedObjectSource(context, mos);
				deployer.link(source.getOfficeFloorManagedObjectFlow("FLOW"), input);
			}

			// Augment the managed object source
			deployer.addManagedObjectSourceAugmentor((augment) -> {

				// Ignore section class object
				assertNotNull("Should have managed object type", augment.getManagedObjectType());
				if (augment.getManagedObjectType().getObjectType().equals(Section.class)) {
					isSectionObjectAvailable.value = true;
					return;
				}

				// Ensure correct name
				assertEquals("Incorrect managed object source name", managedObjectSourceName,
						augment.getManagedObjectSourceName());

				// Obtain the flow
				AugmentedManagedObjectFlow flow = augment.getManagedObjectFlow("FLOW");
				assertEquals("Incorrectly already linked", isAlreadyLinked, flow.isLinked());

				// Undertake possible linking
				if (!flow.isLinked()) {
					assertEquals("Incorrect flow name", "FLOW", flow.getManagedObjectFlowName());
					augment.link(flow, input);
				}
			});
		});
		compile.office((context) -> {
			context.addSection("SERVICE", Section.class);
		});
		if (!isAlreadyLinked) {
			loader.accept(compile, mos);
		}
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Execute the flow (ensures augmented)
			Section.isInvoked = false;
			mos.serviceContext.invokeProcess(Flows.FLOW, null, mos, 0, null);
			assertTrue("Should invoked section", Section.isInvoked);

			// Ensure section object also augmented
			assertTrue("Should augment section object", isSectionObjectAvailable.value);
		}
	}

	public static class Section {

		private static boolean isInvoked = false;

		public void service() {
			isInvoked = true;
		}
	}

	public static enum Flows {
		FLOW
	}

	@TestSource
	public static class AugmentFlowManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject {

		private ManagedObjectServiceContext<Flows> serviceContext;

		/*
		 * =================== ManagedObjectSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addFlow(Flows.FLOW, null);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.serviceContext = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not obtain managed object");
			return null;
		}

		/*
		 * ====================== ManagedObject ==================================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
