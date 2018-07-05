/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.compile.test.managedfunction;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.managedfunction.FunctionNamespaceTypeImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;

/**
 * Utility class for testing a {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLoaderUtil {

	/**
	 * Validates the {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param <S>
	 *            {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends ManagedFunctionSource> PropertyList validateSpecification(
			Class<S> managedFunctionSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null).getManagedFunctionLoader()
				.loadSpecification(managedFunctionSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Validates the {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param managedFunctionSource
	 *            {@link ManagedFunctionSource} instance.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static PropertyList validateSpecification(ManagedFunctionSource managedFunctionSource,
			String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null).getManagedFunctionLoader()
				.loadSpecification(managedFunctionSource);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link FunctionNamespaceBuilder} to create the expected
	 * {@link FunctionNamespaceType}.
	 * 
	 * @return {@link FunctionNamespaceBuilder} to build the expected
	 *         {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceBuilder createManagedFunctionTypeBuilder() {
		return new FunctionNamespaceTypeImpl();
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded
	 * from the input {@link ManagedFunctionSource} against the expected
	 * {@link FunctionNamespaceType} from the {@link FunctionNamespaceBuilder}.
	 * 
	 * @param <S>
	 *            {@link ManagedFunctionSource} type.
	 * @param expectedFunctionNamespaceType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param managedFunctionSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <S extends ManagedFunctionSource> FunctionNamespaceType validateManagedFunctionType(
			FunctionNamespaceBuilder expectedFunctionNamespaceType, Class<S> managedFunctionSourceClass,
			String... propertyNameValues) {
		return validateManagedFunctionType(expectedFunctionNamespaceType, managedFunctionSourceClass, null,
				propertyNameValues);
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded
	 * from the input {@link ManagedFunctionSource} against the expected
	 * {@link FunctionNamespaceType} from the {@link FunctionNamespaceBuilder}.
	 * 
	 * @param <S>
	 *            {@link ManagedFunctionSource} type.
	 * @param expectedFunctionNamespaceType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param managedFunctionSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <S extends ManagedFunctionSource> FunctionNamespaceType validateManagedFunctionType(
			FunctionNamespaceBuilder expectedFunctionNamespaceType, Class<S> managedFunctionSourceClass,
			OfficeFloorCompiler compiler, String... propertyNameValues) {

		// Load the actual namespace
		FunctionNamespaceType aNamespace = loadManagedFunctionType(managedFunctionSourceClass, compiler,
				propertyNameValues);

		// Validate the namespace
		return validateManagedFunctionType(expectedFunctionNamespaceType, aNamespace);
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded
	 * from the input {@link ManagedFunctionSource} against the expected
	 * {@link FunctionNamespaceType} from the {@link FunctionNamespaceBuilder}.
	 * 
	 * @param expectedFunctionNamespaceType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param managedFunctionSource
	 *            {@link ManagedFunctionSource} instance.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceType validateManagedFunctionType(
			FunctionNamespaceBuilder expectedFunctionNamespaceType, ManagedFunctionSource managedFunctionSource,
			String... propertyNameValues) {

		// Load the actual namespace
		FunctionNamespaceType aNamespace = loadManagedFunctionType(managedFunctionSource, propertyNameValues);

		// Validate the namespace
		return validateManagedFunctionType(expectedFunctionNamespaceType, aNamespace);
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded
	 * from the input {@link ManagedFunctionSource} against the expected
	 * {@link FunctionNamespaceType} from the {@link FunctionNamespaceBuilder}.
	 * 
	 * @param expectedFunctionNamespaceType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param aNamespace
	 *            Actual {@link FunctionNamespaceType}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	private static FunctionNamespaceType validateManagedFunctionType(
			FunctionNamespaceBuilder expectedFunctionNamespaceType, FunctionNamespaceType aNamespace) {

		// Cast to obtain expected managed function type
		if (!(expectedFunctionNamespaceType instanceof FunctionNamespaceType)) {
			Assert.fail("expectedFunctionNamespaceType must be created from createManagedFunctionTypeBuilder");
		}
		FunctionNamespaceType eNamespace = (FunctionNamespaceType) expectedFunctionNamespaceType;

		// Ensure have the actual namespace type
		Assert.assertNotNull("Failed to load FunctionNamespaceType", aNamespace);

		// Verify the namespace type
		ManagedFunctionType<?, ?>[] eFunctions = eNamespace.getManagedFunctionTypes();
		ManagedFunctionType<?, ?>[] aFunctions = aNamespace.getManagedFunctionTypes();
		Assert.assertEquals("Incorrect number of functions", eFunctions.length, aFunctions.length);
		for (int i = 0; i < eFunctions.length; i++) {
			ManagedFunctionType<?, ?> eFunction = eFunctions[i];
			ManagedFunctionType<?, ?> aFunction = aFunctions[i];

			// Verify the function type
			Assert.assertEquals("Incorrect function name (function=" + i + ")", eFunction.getFunctionName(),
					aFunction.getFunctionName());
			Assert.assertEquals("Incorrect return type (function=" + i + ")", eFunction.getReturnType(),
					aFunction.getReturnType());
			Assert.assertEquals("Incorrect dependency keys (function=" + eFunction.getFunctionName() + ")",
					eFunction.getObjectKeyClass(), aFunction.getObjectKeyClass());
			Assert.assertEquals("Incorrect flow keys (function=" + eFunction.getFunctionName() + ")",
					eFunction.getFlowKeyClass(), aFunction.getFlowKeyClass());
			Assert.assertNotNull("Must have managed function factory (function=" + eFunction.getFunctionName() + ")",
					aFunction.getManagedFunctionFactory());

			// Verify annotations
			Object[] eAnnotations = eFunction.getAnnotations();
			Object[] aAnnotations = aFunction.getAnnotations();
			Assert.assertEquals("Incorrect number of annotations (function=" + eFunction.getFunctionName() + ")",
					eAnnotations.length, aAnnotations.length);
			for (int d = 0; d < eAnnotations.length; d++) {
				// Match annotation on type
				Assert.assertEquals("Incorrect annotation type (function=" + eFunction.getFunctionName() + ")",
						eAnnotations[d].getClass(), (aAnnotations[d] == null ? null : aAnnotations[d].getClass()));

			}

			// Verify the dependencies
			ManagedFunctionObjectType<?>[] eDependencies = eFunction.getObjectTypes();
			ManagedFunctionObjectType<?>[] aDependencies = aFunction.getObjectTypes();
			Assert.assertEquals("Incorrect number of dependences (function=" + eFunction.getFunctionName() + ")",
					eDependencies.length, aDependencies.length);
			for (int d = 0; d < eDependencies.length; d++) {
				ManagedFunctionObjectType<?> eDependency = eDependencies[d];
				ManagedFunctionObjectType<?> aDependency = aDependencies[d];

				// Verify the dependency
				Assert.assertEquals(
						"Incorrect dependency key (function=" + eFunction.getFunctionName() + ", dependency=" + d + ")",
						eDependency.getKey(), aDependency.getKey());
				Assert.assertEquals("Incorrect dependency type (function=" + eFunction.getFunctionName()
						+ ", dependency=" + d + ")", eDependency.getObjectType(), aDependency.getObjectType());
				Assert.assertEquals("Incorrect dependency qualifier (function=" + eFunction.getFunctionName()
						+ ", dependency=" + d + ")", eDependency.getTypeQualifier(), aDependency.getTypeQualifier());
				Assert.assertEquals("Incorrect dependency index (function=" + eFunction.getFunctionName()
						+ ", dependency=" + d + ")", eDependency.getIndex(), aDependency.getIndex());
				Assert.assertEquals("Incorrect dependency name (function=" + eFunction.getFunctionName()
						+ ", dependency=" + d + ")", eDependency.getObjectName(), aDependency.getObjectName());
			}

			// Verify the flows
			ManagedFunctionFlowType<?>[] eFlows = eFunction.getFlowTypes();
			ManagedFunctionFlowType<?>[] aFlows = aFunction.getFlowTypes();
			Assert.assertEquals("Incorrect number of flows (function=" + eFunction.getFunctionName() + ")",
					eFlows.length, aFlows.length);
			for (int f = 0; f < eFlows.length; f++) {
				ManagedFunctionFlowType<?> eFlow = eFlows[f];
				ManagedFunctionFlowType<?> aFlow = aFlows[f];

				// Verify the flow
				Assert.assertEquals("Incorrect flow key (function=" + eFunction.getFunctionName() + ", flow=" + f + ")",
						eFlow.getKey(), aFlow.getKey());
				Assert.assertEquals(
						"Incorrect flow argument type (function=" + eFunction.getFunctionName() + ", flow=" + f + ")",
						eFlow.getArgumentType(), aFlow.getArgumentType());
				Assert.assertEquals(
						"Incorrect flow index (function=" + eFunction.getFunctionName() + ", flow=" + f + ")",
						eFlow.getIndex(), aFlow.getIndex());
				Assert.assertEquals(
						"Incorrect flow name (function=" + eFunction.getFunctionName() + ", flow=" + f + ")",
						eFlow.getFlowName(), aFlow.getFlowName());
			}

			// Verify the escalations
			ManagedFunctionEscalationType[] eEscalations = eFunction.getEscalationTypes();
			ManagedFunctionEscalationType[] aEscalations = aFunction.getEscalationTypes();
			Assert.assertEquals("Incorrect number of escalations (function=" + i + ")", eEscalations.length,
					aEscalations.length);
			for (int e = 0; e < eEscalations.length; e++) {
				ManagedFunctionEscalationType eEscalation = eEscalations[e];
				ManagedFunctionEscalationType aEscalation = aEscalations[e];

				// Verify the escalation
				Assert.assertEquals("Incorrect escalation type (function=" + eFunction.getFunctionName()
						+ ", escalation=" + e + ")", eEscalation.getEscalationType(), aEscalation.getEscalationType());
				Assert.assertEquals("Incorrect escalation name (function=" + eFunction.getFunctionName()
						+ ", escalation=" + e + ")", eEscalation.getEscalationName(), aEscalation.getEscalationName());
			}
		}

		// Return the actual namespace type
		return aNamespace;
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} by obtaining
	 * the {@link ClassLoader} from the {@link ManagedFunctionSource} class.
	 * 
	 * @param <S>
	 *            {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(
			Class<S> managedFunctionSourceClass, String... propertyNameValues) {
		// Return the loaded namespace
		return loadManagedFunctionType(managedFunctionSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} with the
	 * provided {@link OfficeFloorCompiler}.
	 * 
	 * @param <S>
	 *            {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(
			Class<S> managedFunctionSourceClass, OfficeFloorCompiler compiler, String... propertyNameValues) {
		// Return the loaded namespace type
		return getOfficeFloorCompiler(compiler).getManagedFunctionLoader()
				.loadManagedFunctionType(managedFunctionSourceClass, new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} by obtaining
	 * the {@link ClassLoader} from the {@link ManagedFunctionSource} class.
	 * 
	 * @param managedFunctionSource
	 *            {@link ManagedFunctionSource} instance.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for the
	 *            {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceType loadManagedFunctionType(ManagedFunctionSource managedFunctionSource,
			String... propertyNameValues) {
		// Return the loaded namespace type
		return getOfficeFloorCompiler(null).getManagedFunctionLoader().loadManagedFunctionType(managedFunctionSource,
				new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(OfficeFloorCompiler compiler) {
		if (compiler == null) {
			// Create the office floor compiler that fails on first issue
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private ManagedFunctionLoaderUtil() {
	}

}