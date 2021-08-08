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
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.annotation.AnnotationLoaderUtil;
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
	 * @param <S>                        {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @param propertyNameLabels         Listing of name/label pairs for the
	 *                                   {@link Property} instances.
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
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @param propertyNameLabels    Listing of name/label pairs for the
	 *                              {@link Property} instances.
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
	 * @param <S>                           {@link ManagedFunctionSource} type.
	 * @param expectedFunctionNamespaceType {@link FunctionNamespaceBuilder} that
	 *                                      has had the expected
	 *                                      {@link FunctionNamespaceType} built
	 *                                      against it.
	 * @param managedFunctionSourceClass    {@link ManagedFunctionSource} class.
	 * @param propertyNameValues            Listing of name/value pairs that
	 *                                      comprise the properties for the
	 *                                      {@link ManagedFunctionSource}.
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
	 * @param <S>                           {@link ManagedFunctionSource} type.
	 * @param expectedFunctionNamespaceType {@link FunctionNamespaceBuilder} that
	 *                                      has had the expected
	 *                                      {@link FunctionNamespaceType} built
	 *                                      against it.
	 * @param managedFunctionSourceClass    {@link ManagedFunctionSource} class.
	 * @param compiler                      {@link OfficeFloorCompiler}. May be
	 *                                      <code>null</code>.
	 * @param propertyNameValues            Listing of name/value pairs that
	 *                                      comprise the properties for the
	 *                                      {@link ManagedFunctionSource}.
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
	 * @param expectedFunctionNamespaceType {@link FunctionNamespaceBuilder} that
	 *                                      has had the expected
	 *                                      {@link FunctionNamespaceType} built
	 *                                      against it.
	 * @param managedFunctionSource         {@link ManagedFunctionSource} instance.
	 * @param propertyNameValues            Listing of name/value pairs that
	 *                                      comprise the properties for the
	 *                                      {@link ManagedFunctionSource}.
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
	 * @param expectedFunctionNamespaceType {@link FunctionNamespaceBuilder} that
	 *                                      has had the expected
	 *                                      {@link FunctionNamespaceType} built
	 *                                      against it.
	 * @param aNamespace                    Actual {@link FunctionNamespaceType}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceType validateManagedFunctionType(
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
			validateManagedFunctionType(eFunction, aFunction);
		}

		// Return the actual namespace type
		return aNamespace;
	}

	/**
	 * Validates a specific {@link ManagedFunctionType}.
	 * 
	 * @param expectedFunction {@link ManagedFunctionTypeBuilder} that has had the
	 *                         expected {@link ManagedFunctionType} built against
	 *                         it.
	 * @param actualFunction   Actual {@link ManagedFunctionType}.
	 */
	public static void validateManagedFunctionType(ManagedFunctionTypeBuilder<?, ?> expectedFunction,
			ManagedFunctionType<?, ?> actualFunction) {

		// Cast to obtain expected managed function type
		if (!(expectedFunction instanceof ManagedFunctionType)) {
			Assert.fail("expectedFunction must be created from createManagedFunctionTypeBuilder");
		}
		ManagedFunctionType<?, ?> eFunction = (ManagedFunctionType<?, ?>) expectedFunction;

		// Ensure correct function
		Assert.assertEquals("Incorrect function name", eFunction.getFunctionName(), actualFunction.getFunctionName());

		// Validate the managed function
		validateManagedFunctionType(eFunction, actualFunction);
	}

	/**
	 * Validates the {@link ManagedFunctionType}.
	 * 
	 * @param eFunction Expected {@link ManagedFunctionType}.
	 * @param aFunction Actual {@link ManagedFunctionType}.
	 */
	private static void validateManagedFunctionType(ManagedFunctionType<?, ?> eFunction,
			ManagedFunctionType<?, ?> aFunction) {

		Assert.assertEquals("Incorrect return type (function=" + eFunction.getFunctionName() + ")",
				eFunction.getReturnType(), aFunction.getReturnType());
		Assert.assertEquals("Incorrect dependency keys (function=" + eFunction.getFunctionName() + ")",
				eFunction.getObjectKeyClass(), aFunction.getObjectKeyClass());
		Assert.assertEquals("Incorrect flow keys (function=" + eFunction.getFunctionName() + ")",
				eFunction.getFlowKeyClass(), aFunction.getFlowKeyClass());
		Assert.assertNotNull("Must have managed function factory (function=" + eFunction.getFunctionName() + ")",
				aFunction.getManagedFunctionFactory());

		// Verify annotations
		AnnotationLoaderUtil.validateAnnotations("for function " + eFunction.getFunctionName(),
				eFunction.getAnnotations(), aFunction.getAnnotations());

		// Verify the dependencies
		ManagedFunctionObjectType<?>[] eDependencies = eFunction.getObjectTypes();
		ManagedFunctionObjectType<?>[] aDependencies = aFunction.getObjectTypes();
		Assert.assertEquals("Incorrect number of dependences (function=" + eFunction.getFunctionName() + ")",
				eDependencies.length, aDependencies.length);
		for (int d = 0; d < eDependencies.length; d++) {
			ManagedFunctionObjectType<?> eDependency = eDependencies[d];
			ManagedFunctionObjectType<?> aDependency = aDependencies[d];
			String suffix = " (function=" + eFunction.getFunctionName() + ", dependency=" + d + ")";

			// Verify the dependency
			Assert.assertEquals("Incorrect dependency" + suffix, eDependency.getKey(), aDependency.getKey());
			Assert.assertEquals("Incorrect dependency type" + suffix, eDependency.getObjectType(),
					aDependency.getObjectType());
			Assert.assertEquals("Incorrect dependency qualifier" + suffix, eDependency.getTypeQualifier(),
					aDependency.getTypeQualifier());
			Assert.assertEquals("Incorrect dependency index" + suffix, eDependency.getIndex(), aDependency.getIndex());
			Assert.assertEquals("Incorrect dependency name" + suffix, eDependency.getObjectName(),
					aDependency.getObjectName());

			// Verify expected annotations exist
			AnnotationLoaderUtil.validateAnnotations(suffix, eDependency.getAnnotations(),
					aDependency.getAnnotations());
		}

		// Verify the flows
		ManagedFunctionFlowType<?>[] eFlows = eFunction.getFlowTypes();
		ManagedFunctionFlowType<?>[] aFlows = aFunction.getFlowTypes();
		Assert.assertEquals("Incorrect number of flows (function=" + eFunction.getFunctionName() + ")", eFlows.length,
				aFlows.length);
		for (int f = 0; f < eFlows.length; f++) {
			ManagedFunctionFlowType<?> eFlow = eFlows[f];
			ManagedFunctionFlowType<?> aFlow = aFlows[f];
			String suffix = " (function=" + eFunction.getFunctionName() + ", flow=" + f + ")";

			// Verify the flow
			Assert.assertEquals("Incorrect flow key " + suffix, eFlow.getKey(), aFlow.getKey());
			Assert.assertEquals("Incorrect flow argument type " + suffix, eFlow.getArgumentType(),
					aFlow.getArgumentType());
			Assert.assertEquals("Incorrect flow index " + suffix, eFlow.getIndex(), aFlow.getIndex());
			Assert.assertEquals("Incorrect flow name " + suffix, eFlow.getFlowName(), aFlow.getFlowName());

			// Verify expected annotations exist
			AnnotationLoaderUtil.validateAnnotations(suffix, eFlow.getAnnotations(), aFlow.getAnnotations());
		}

		// Verify the escalations
		ManagedFunctionEscalationType[] eEscalations = eFunction.getEscalationTypes();
		ManagedFunctionEscalationType[] aEscalations = aFunction.getEscalationTypes();
		Assert.assertEquals("Incorrect number of escalations (function=" + eFunction.getFunctionName() + ")",
				eEscalations.length, aEscalations.length);
		for (int e = 0; e < eEscalations.length; e++) {
			ManagedFunctionEscalationType eEscalation = eEscalations[e];
			ManagedFunctionEscalationType aEscalation = aEscalations[e];
			String suffix = " (function=" + eFunction.getFunctionName() + ", escalation=" + e + ")";

			// Verify the escalation
			Assert.assertEquals("Incorrect escalation type " + suffix, eEscalation.getEscalationType(),
					aEscalation.getEscalationType());
			Assert.assertEquals("Incorrect escalation name " + suffix, eEscalation.getEscalationName(),
					aEscalation.getEscalationName());
		}
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} by obtaining
	 * the {@link ClassLoader} from the {@link ManagedFunctionSource} class.
	 * 
	 * @param <S>                        {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @param propertyNameValues         Listing of name/value pairs that comprise
	 *                                   the properties for the
	 *                                   {@link ManagedFunctionSource}.
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
	 * @param <S>                        {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @param compiler                   {@link OfficeFloorCompiler}.
	 * @param propertyNameValues         Listing of name/value pairs that comprise
	 *                                   the properties for the
	 *                                   {@link ManagedFunctionSource}.
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
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @param propertyNameValues    Listing of name/value pairs that comprise the
	 *                              properties for the
	 *                              {@link ManagedFunctionSource}.
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
	 * @param compiler {@link OfficeFloorCompiler}. May be <code>null</code>.
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
