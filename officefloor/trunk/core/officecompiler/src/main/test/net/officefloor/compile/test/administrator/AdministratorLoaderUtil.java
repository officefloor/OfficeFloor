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
package net.officefloor.compile.test.administrator;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyFlowType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * Utility class for testing the {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorLoaderUtil {

	/**
	 * Validates the {@link AdministratorSourceSpecification} for the
	 * {@link AdministratorSource}.
	 * 
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <I, A extends Enum<A>, S extends AdministratorSource<I, A>> PropertyList validateSpecification(
			Class<S> administratorSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler()
				.getAdministratorLoader().loadSpecification(
						administratorSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link AdministratorTypeBuilder} to create the expected
	 * {@link AdministratorType}.
	 * 
	 * @return {@link AdministratorTypeBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	public static AdministratorTypeBuilder createAdministratorTypeBuilder() {
		return new AdministratorTypeBuilderImpl();
	}

	/**
	 * Validates the {@link AdministratorType} contained in the
	 * {@link AdministratorTypeBuilder} against the {@link AdministratorType}
	 * loaded from the {@link AdministratorSource}.
	 * 
	 * @return {@link AdministratorType} loaded from the
	 *         {@link AdministratorSource}.
	 */
	@SuppressWarnings("unchecked")
	public static <I, A extends Enum<A>, S extends AdministratorSource<I, A>> AdministratorType<I, A> validateAdministratorType(
			AdministratorTypeBuilder expectedAdministratorType,
			Class<S> administratorSourceClass, String... propertyNameValues) {

		// Cast to obtain expected administrator type
		if (!(expectedAdministratorType instanceof AdministratorType)) {
			TestCase.fail("builder must be created from createAdministratorTypeBuilder");
		}
		AdministratorType<I, A> eType = (AdministratorType<I, A>) expectedAdministratorType;

		// Load the administrator type
		AdministratorType<I, A> aType = loadAdministratorType(
				administratorSourceClass, propertyNameValues);

		// Ensure correct administrator type
		TestCase.assertEquals("Incorrect extension interface type",
				eType.getExtensionInterface(), aType.getExtensionInterface());

		// Validate the duties
		DutyType<A, ?>[] eDuties = eType.getDutyTypes();
		DutyType<A, ?>[] aDuties = aType.getDutyTypes();
		TestCase.assertEquals("Incorrect number of duties", eDuties.length,
				aDuties.length);
		for (int d = 0; d < eDuties.length; d++) {
			DutyType<A, ?> eDuty = eType.getDutyTypes()[d];
			DutyType<A, ?> aDuty = aType.getDutyTypes()[d];

			// Validate the duty
			TestCase.assertEquals("Incorrect name for duty " + d,
					eDuty.getDutyName(), aDuty.getDutyName());
			TestCase.assertEquals("Incorrect key for duty " + d,
					eDuty.getDutyKey(), aDuty.getDutyKey());
			TestCase.assertEquals("Incorrect flow key class " + d,
					eDuty.getFlowKeyClass(), aDuty.getFlowKeyClass());

			// Validate the flows
			DutyFlowType<?>[] eFlows = eDuty.getFlowTypes();
			DutyFlowType<?>[] aFlows = aDuty.getFlowTypes();
			for (int f = 0; f < eFlows.length; f++) {
				DutyFlowType<?> eFlow = eFlows[f];
				DutyFlowType<?> aFlow = aFlows[f];

				String flowLabel = "flow" + f + " (duty " + d + ")";

				// Validate the flow
				TestCase.assertEquals("Incorrect name for " + flowLabel,
						eFlow.getFlowName(), aFlow.getFlowName());
				TestCase.assertEquals("Incorrect index for " + flowLabel,
						eFlow.getIndex(), aFlow.getIndex());
				TestCase.assertEquals("Incorrect key for " + flowLabel,
						eFlow.getKey(), aFlow.getKey());
				TestCase.assertEquals("Incorrect argument type for "
						+ flowLabel, eFlow.getArgumentType(),
						aFlow.getArgumentType());
			}
		}

		// Return the administrator type
		return aType;
	}

	/**
	 * Loads the {@link AdministratorType} from the {@link AdministratorSource}.
	 * 
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link AdministratorType}.
	 */
	public static <I, A extends Enum<A>, S extends AdministratorSource<I, A>> AdministratorType<I, A> loadAdministratorType(
			Class<S> administratorSourceClass, String... propertyNameValues) {

		// Load and return the administrator type
		return getOfficeFloorCompiler().getAdministratorLoader()
				.loadAdministrator(administratorSourceClass,
						new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the office floor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private AdministratorLoaderUtil() {
	}

	/**
	 * {@link AdministratorTypeBuilder} implementation.
	 */
	private static class AdministratorTypeBuilderImpl<I, A extends Enum<A>>
			implements AdministratorTypeBuilder, AdministratorType<I, A> {

		/**
		 * Extension interface.
		 */
		private Class<I> extensionInterface;

		/**
		 * {@link DutyType} instances.
		 */
		private final List<DutyType<A, ?>> duties = new LinkedList<DutyType<A, ?>>();

		/*
		 * ================ AdministratorTypeBuilder ======================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setExtensionInterface(Class extensionInterface) {
			this.extensionInterface = extensionInterface;
		}

		@Override
		@SuppressWarnings("unchecked")
		public DutyTypeBuilder<Indexed> addDuty(String dutyName, Enum<?> dutyKey) {
			DutyTypeBuilderImpl<A, Indexed> duty = new DutyTypeBuilderImpl<A, Indexed>(
					dutyName, (A) dutyKey, null);
			this.duties.add(duty);
			return duty;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F>> DutyTypeBuilder<F> addDuty(String dutyName,
				Enum<?> dutyKey, Class<F> flowKeyClass) {
			DutyTypeBuilderImpl<A, F> duty = new DutyTypeBuilderImpl<A, F>(
					dutyName, (A) dutyKey, flowKeyClass);
			this.duties.add(duty);
			return duty;
		}

		/*
		 * ================== AdministratorType ==========================
		 */

		@Override
		public Class<I> getExtensionInterface() {
			return this.extensionInterface;
		}

		@Override
		public DutyType<A, ?>[] getDutyTypes() {
			return CompileUtil.toArray(this.duties, new DutyType[0]);
		}
	}

	/**
	 * {@link DutyTypeBuilder} implementation.
	 */
	private static class DutyTypeBuilderImpl<A extends Enum<A>, F extends Enum<F>>
			implements DutyTypeBuilder<F>, DutyType<A, F> {

		/**
		 * Name of the {@link Duty}.
		 */
		private final String dutyName;

		/**
		 * Key identifying the {@link Duty}.
		 */
		private final A dutyKey;

		/**
		 * {@link JobSequence} {@link Enum}.
		 */
		private final Class<F> flowKeyClass;

		/**
		 * Listing of {@link DutyFlowType} instances.
		 */
		private final List<DutyFlowType<F>> flows = new LinkedList<DutyFlowType<F>>();

		/**
		 * Initiate.
		 * 
		 * @param dutyName
		 *            Name of the {@link Duty}.
		 * @param dutyKey
		 *            Key identifying the {@link Duty}.
		 * @param flowKeyClass
		 *            {@link JobSequence} {@link Enum}.
		 */
		public DutyTypeBuilderImpl(String dutyName, A dutyKey,
				Class<F> flowKeyClass) {
			this.dutyName = dutyName;
			this.dutyKey = dutyKey;
			this.flowKeyClass = flowKeyClass;
		}

		/*
		 * ================ DutyTypeBuilder ===============================
		 */

		@Override
		public void addFlow(String flowName, Class<?> argumentType, int index,
				F flowKey) {
			this.flows.add(new DutyFlowTypeImpl<F>(flowName, argumentType,
					index, flowKey));
		}

		/*
		 * ======================= DutyType ===========================
		 */

		@Override
		public String getDutyName() {
			return this.dutyName;
		}

		@Override
		public A getDutyKey() {
			return this.dutyKey;
		}

		@Override
		public Class<F> getFlowKeyClass() {
			return this.flowKeyClass;
		}

		@Override
		public DutyFlowType<F>[] getFlowTypes() {
			return CompileUtil.toArray(this.flows, new DutyFlowType[0]);
		}
	}

	/**
	 * {@link DutyFlowType} implementation.
	 */
	private static class DutyFlowTypeImpl<F extends Enum<F>> implements
			DutyFlowType<F> {

		/**
		 * Name of the {@link JobSequence}.
		 */
		private final String flowName;

		/**
		 * Argument type to the {@link JobSequence}.
		 */
		private final Class<?> argumentType;

		/**
		 * Index identifying the {@link JobSequence}.
		 */
		private final int index;

		/**
		 * Key identifying the {@link JobSequence}.
		 */
		private final F key;

		/**
		 * Initiate.
		 * 
		 * @param flowName
		 *            Name of the {@link JobSequence}.
		 * @param argumentType
		 *            Argument type to the {@link JobSequence}.
		 * @param index
		 *            Index identifying the {@link JobSequence}.
		 * @param key
		 *            Key identifying the {@link JobSequence}.
		 */
		public DutyFlowTypeImpl(String flowName, Class<?> argumentType,
				int index, F key) {
			this.flowName = flowName;
			this.argumentType = argumentType;
			this.index = index;
			this.key = key;
		}

		/*
		 * ==================== DutyFlowType =======================
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

}