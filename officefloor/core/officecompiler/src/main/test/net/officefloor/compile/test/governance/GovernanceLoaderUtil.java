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

package net.officefloor.compile.test.governance;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Utility class for testing the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceLoaderUtil {

	/**
	 * Validates the {@link GovernanceSourceSpecification} for the
	 * {@link GovernanceSource}.
	 * 
	 * @param <I>                   Extension interface type.
	 * @param <F>                   {@link Flow} type keys.
	 * @param <S>                   {@link GovernanceSource} type.
	 * @param governanceSourceClass {@link GovernanceSource} class.
	 * @param propertyNameLabels    Listing of name/label pairs for the
	 *                              {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <I, F extends Enum<F>, S extends GovernanceSource<I, F>> PropertyList validateSpecification(
			Class<S> governanceSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler().getGovernanceLoader()
				.loadSpecification(governanceSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link GovernanceTypeBuilder} to create the expected
	 * {@link GovernanceType}.
	 * 
	 * @return {@link GovernanceTypeBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	public static GovernanceTypeBuilder createGovernanceTypeBuilder() {
		return new GovernanceTypeBuilderImpl();
	}

	/**
	 * Validates the {@link GovernanceType} contained in the
	 * {@link GovernanceTypeBuilder} against the {@link GovernanceType} loaded from
	 * the {@link GovernanceSource}.
	 * 
	 * @param <I>                    Extension interface type.
	 * @param <F>                    {@link Flow} type keys.
	 * @param <S>                    {@link GovernanceSource} type.
	 * @param expectedGovernanceType Expected {@link GovernanceType}.
	 * @param governanceSourceClass  {@link GovernanceSource} class.
	 * @param propertyNameValues     Properties to configure the
	 *                               {@link GovernanceSource}.
	 * @return {@link GovernanceType} loaded from the {@link GovernanceSource}.
	 */
	@SuppressWarnings("unchecked")
	public static <I, F extends Enum<F>, S extends GovernanceSource<I, F>> GovernanceType<I, F> validateGovernanceType(
			GovernanceTypeBuilder<?> expectedGovernanceType, Class<S> governanceSourceClass,
			String... propertyNameValues) {

		// Cast to obtain expected governance type
		if (!(expectedGovernanceType instanceof GovernanceType)) {
			TestCase.fail("builder must be created from createGovernanceTypeBuilder");
		}
		GovernanceType<I, F> eType = (GovernanceType<I, F>) expectedGovernanceType;

		// Load the governance type
		GovernanceType<I, F> aType = loadGovernanceType(governanceSourceClass, propertyNameValues);

		// Ensure correct governance type
		TestCase.assertNotNull("Must have GovernanceFactory", aType.getGovernanceFactory());
		TestCase.assertEquals("Incorrect extension interface type", eType.getExtensionType(), aType.getExtensionType());

		// Validate the flows
		GovernanceFlowType<?>[] eFlows = eType.getFlowTypes();
		GovernanceFlowType<?>[] aFlows = aType.getFlowTypes();
		for (int f = 0; f < eFlows.length; f++) {
			GovernanceFlowType<?> eFlow = eFlows[f];
			GovernanceFlowType<?> aFlow = aFlows[f];

			// Validate the flow
			TestCase.assertEquals("Incorrect name for flow " + f, eFlow.getFlowName(), aFlow.getFlowName());
			TestCase.assertEquals("Incorrect index for flow " + f, eFlow.getIndex(), aFlow.getIndex());
			TestCase.assertEquals("Incorrect key for flow " + f, eFlow.getKey(), aFlow.getKey());
			TestCase.assertEquals("Incorrect argument type for flow " + f, eFlow.getArgumentType(),
					aFlow.getArgumentType());
		}

		// Validate the escalations
		GovernanceEscalationType[] eEscalations = eType.getEscalationTypes();
		GovernanceEscalationType[] aEscalations = aType.getEscalationTypes();
		for (int e = 0; e < eFlows.length; e++) {
			GovernanceEscalationType eEscalation = eEscalations[e];
			GovernanceEscalationType aEscalation = aEscalations[e];

			// Validate the escalation
			TestCase.assertEquals("Incorrect name for escalation " + e, eEscalation.getEscalationName(),
					aEscalation.getEscalationName());
			TestCase.assertEquals("Incorrect type for escalation " + e, eEscalation.getEscalationType(),
					aEscalation.getEscalationType());
		}

		// Return the governance type
		return aType;
	}

	/**
	 * Loads the {@link GovernanceType} from the {@link GovernanceSource}.
	 * 
	 * @param <I>                   Extension interface type.
	 * @param <F>                   {@link Flow} type keys.
	 * @param <S>                   {@link GovernanceSource} type.
	 * @param governanceSourceClass {@link GovernanceSource} class.
	 * @param propertyNameValues    {@link Property} name/value listing.
	 * @return {@link GovernanceType}.
	 */
	public static <I, F extends Enum<F>, S extends GovernanceSource<I, F>> GovernanceType<I, F> loadGovernanceType(
			Class<S> governanceSourceClass, String... propertyNameValues) {

		// Load and return the administrator type
		return getOfficeFloorCompiler().getGovernanceLoader().loadGovernanceType(governanceSourceClass,
				new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the office floor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private GovernanceLoaderUtil() {
	}

	/**
	 * {@link GovernanceTypeBuilder} implementation.
	 */
	private static class GovernanceTypeBuilderImpl<I, F extends Enum<F>>
			implements GovernanceTypeBuilder<F>, GovernanceType<I, F> {

		/**
		 * Extension interface.
		 */
		private Class<?> extensionInterface;

		/**
		 * {@link GovernanceFlowType} instances.
		 */
		private List<GovernanceFlowType<F>> flows = new LinkedList<GovernanceFlowType<F>>();

		/**
		 * {@link GovernanceEscalationType} instances.
		 */
		private List<GovernanceEscalationType> escalations = new LinkedList<GovernanceEscalationType>();

		/*
		 * ==================== GovernanceTypeBuilder ====================
		 */

		@Override
		public void setExtensionInterface(Class<?> extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		@Override
		public void addFlow(String flowName, Class<?> argumentType, int index, F flowKey) {
			this.flows.add(new GovernanceFlowTypeImpl<F>(flowName, argumentType, index, flowKey));
		}

		@Override
		public void addEscalation(Class<?> escalationType) {
			this.escalations.add(new GovernanceEscalationTypeImpl(escalationType));
		}

		/*
		 * ======================= GovernanceType ==============================
		 */

		@Override
		public GovernanceFactory<? extends I, F> getGovernanceFactory() {
			TestCase.fail("GovernanceFactory of type builder should not be checked");
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<I> getExtensionType() {
			return (Class<I>) this.extensionInterface;
		}

		@Override
		@SuppressWarnings("unchecked")
		public GovernanceFlowType<F>[] getFlowTypes() {
			return this.flows.toArray(new GovernanceFlowType[this.flows.size()]);
		}

		@Override
		public GovernanceEscalationType[] getEscalationTypes() {
			return this.escalations.toArray(new GovernanceEscalationType[this.escalations.size()]);
		}
	}

	/**
	 * {@link GovernanceFlowType} implementation.
	 */
	private static class GovernanceFlowTypeImpl<F extends Enum<F>> implements GovernanceFlowType<F> {

		/**
		 * Name of the {@link Flow}.
		 */
		private final String flowName;

		/**
		 * Argument type to the {@link Flow}.
		 */
		private final Class<?> argumentType;

		/**
		 * Index identifying the {@link Flow}.
		 */
		private final int index;

		/**
		 * Key identifying the {@link Flow}.
		 */
		private final F key;

		/**
		 * Initiate.
		 * 
		 * @param flowName     Name of the {@link Flow}.
		 * @param argumentType Argument type to the {@link Flow}.
		 * @param index        Index identifying the {@link Flow}.
		 * @param key          Key identifying the {@link Flow}.
		 */
		public GovernanceFlowTypeImpl(String flowName, Class<?> argumentType, int index, F key) {
			this.flowName = flowName;
			this.argumentType = argumentType;
			this.index = index;
			this.key = key;
		}

		/*
		 * ==================== GovernanceFlowType =======================
		 */

		@Override
		public String getFlowName() {
			return this.flowName;
		}

		@Override
		public Class<?> getArgumentType() {
			return this.argumentType;
		}

		@Override
		public int getIndex() {
			return this.index;
		}

		@Override
		public F getKey() {
			return this.key;
		}
	}

	/**
	 * {@link GovernanceEscalationType} implementation.
	 */
	private static class GovernanceEscalationTypeImpl implements GovernanceEscalationType {

		/**
		 * {@link Escalation} type.
		 */
		private final Class<?> escalationType;

		/**
		 * Initiate.
		 * 
		 * @param escalationType {@link Escalation} type.
		 */
		public GovernanceEscalationTypeImpl(Class<?> escalationType) {
			this.escalationType = escalationType;
		}

		/*
		 * ================= GovernanceEscalationType =================
		 */

		@Override
		public String getEscalationName() {
			return this.escalationType.getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> Class<E> getEscalationType() {
			return (Class<E>) this.escalationType;
		}
	}

}
