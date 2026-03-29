/*-
 * #%L
 * Polyglot Test
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

package net.officefloor.polyglot.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureTypeBuilder;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.test.variable.MockVar;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.woof.mock.MockObjectResponse;

/**
 * Abstract tests for a polyglot {@link Procedure} via a {@link SectionSource}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPolyglotProcedureTest extends OfficeFrameTestCase {

	/**
	 * {@link ObjectMapper}.
	 */
	protected static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Convenience method to add a {@link Procedure}.
	 *
	 * @param resourceName   Name of resource.
	 * @param serviceFactory {@link ProcedureSourceServiceFactory}.
	 * @param procedureName  Name of {@link Procedure}.
	 * @param isNext         Indicates if next {@link Flow}.
	 * @param properties     {@link PropertyList}.
	 * @param context        {@link CompileOfficeContext}.
	 * @return {@link OfficeSection} for the {@link Procedure}.
	 */
	protected OfficeSection addProcedure(String resourceName,
			Class<? extends ProcedureSourceServiceFactory> serviceFactory, String procedureName, boolean isNext,
			PropertyList properties, CompileOfficeContext context) {

		// Load the source
		String sourceName = this.getSourceName(serviceFactory);

		// Obtain the procedure architect
		ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
				.employProcedureArchitect(context.getOfficeArchitect(), context.getOfficeSourceContext());

		// Create and return procedure
		return procedureArchitect.addProcedure(procedureName, resourceName, sourceName, procedureName, isNext,
				properties);
	}

	/**
	 * Obtains the {@link ProcedureSourceServiceFactory} {@link Class}.
	 *
	 * @return {@link ProcedureSourceServiceFactory} {@link Class}.
	 */
	protected abstract Class<? extends ProcedureSourceServiceFactory> getProcedureSourceServiceFactoryClass();

	/**
	 * Obtains the source name for the {@link ProcedureSourceServiceFactory}.
	 *
	 * @param serviceFactory {@link ProcedureSourceServiceFactory} {@link Class}.
	 * @return Source name.
	 */
	protected String getSourceName(Class<? extends ProcedureSourceServiceFactory> serviceFactory) {
		ProcedureSource source = ProcedureLoaderUtil.loadProcedureSource(serviceFactory);
		return source.getSourceName();
	}

	/**
	 * Some languages do not provide exceptions.
	 *
	 * @return <code>true</code> if support {@link Exception}.
	 */
	protected boolean isSupportExceptions() {
		return true; // support by default
	}

	/**
	 * Builds the {@link Procedure}.
	 */
	protected interface ProcedureBuilder extends PropertyConfigurable {

		/**
		 * Convenience method to configure the {@link Procedure}.
		 *
		 * @param resourceClass Resource {@link Class}.
		 * @param procedureName Name of {@link Procedure}.
		 */
		void setProcedure(Class<?> resourceClass, String procedureName);

		/**
		 * Convenience method to configure the {@link Procedure}.
		 *
		 * @param resource      Resource.
		 * @param procedureName Name of {@link Procedure}.
		 */
		void setProcedure(String resource, String procedureName);

		/**
		 * Specifies the resource.
		 *
		 * @param resource Resource.
		 */
		void setResource(String resource);

		/**
		 * Specifies an override {@link ProcedureSourceServiceFactory} {@link Class}.
		 *
		 * @param serviceFactoryClass Override {@link ProcedureSourceServiceFactory}
		 *                            {@link Class}.
		 */
		void setProcedureSourceServiceFactoryClass(Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass);

		/**
		 * Specifies the name of the {@link Procedure}.
		 *
		 * @param procedureName Name of the {@link Procedure}.
		 */
		void setProcedureName(String procedureName);

		/**
		 * Allows overriding {@link ManagedFunction} name derived from {@link Procedure}
		 * name.
		 *
		 * @param managedFunctionName Overriding {@link ManagedFunction} name.
		 */
		void setManagedFunctionName(String managedFunctionName);
	}

	/**
	 * {@link ProcedureBuilder} implementation.
	 */
	private class ProcedureBuildImpl implements ProcedureBuilder {

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList properties = new PropertyListImpl();

		/**
		 * Resource.
		 */
		private String resource = null;

		/**
		 * {@link ProcedureSourceServiceFactory} {@link Class}.
		 */
		private Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass = null;

		/**
		 * Name of the {@link Procedure}.
		 */
		private String procedureName = null;

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private String managedFunctionName = null;

		/**
		 * Obtains the resource.
		 *
		 * @return Resource.
		 */
		private String getResource() {
			assertNotNull("Must specify procedure resource", this.resource);
			return this.resource;
		}

		/**
		 * Obtains the {@link ProcedureSourceServiceFactory} {@link Class}.
		 *
		 * @return {@link ProcedureSourceServiceFactory} {@link Class}.
		 */
		private Class<? extends ProcedureSourceServiceFactory> getProcedureSourceServiceFactory() {
			return this.serviceFactoryClass != null ? this.serviceFactoryClass
					: AbstractPolyglotProcedureTest.this.getProcedureSourceServiceFactoryClass();
		}

		/**
		 * Obtains the {@link Procedure} name.
		 *
		 * @return {@link Procedure} name.
		 */
		private String getProcedureName() {
			assertNotNull("Must specify procedure name", this.procedureName);
			return this.procedureName;
		}

		/**
		 * Obtains the {@link PropertyList}.
		 * 
		 * @return {@link PropertyList}.
		 */
		private PropertyList getProperties() {
			return this.properties;
		}

		/**
		 * Obtains the {@link ManagedFunction} name.
		 *
		 * @return {@link ManagedFunction} name.
		 */
		private String getManagedFunctionName() {
			return this.managedFunctionName != null ? this.managedFunctionName : this.getProcedureName() + ".procedure";
		}

		/*
		 * ===================== ProcedureBuilder ==========================
		 */

		@Override
		public void setProcedure(Class<?> resourceClass, String procedureName) {
			this.setProcedure(resourceClass.getName(), procedureName);
		}

		@Override
		public void setProcedure(String resource, String procedureName) {
			this.setResource(resource);
			this.setProcedureName(procedureName);
		}

		@Override
		public void setResource(String resource) {
			this.resource = resource;
		}

		@Override
		public void setProcedureSourceServiceFactoryClass(
				Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass) {
			this.serviceFactoryClass = serviceFactoryClass;
		}

		@Override
		public void setProcedureName(String procedureName) {
			this.procedureName = procedureName;
		}

		@Override
		public void addProperty(String name, String value) {
			this.properties.addProperty(name).setValue(value);
		}

		@Override
		public void setManagedFunctionName(String managedFunctionName) {
			this.managedFunctionName = managedFunctionName;
		}
	}

	/**
	 * Validates the {@link ProcedureType}.
	 *
	 * @param expected Expected {@link ProcedureTypeBuilder}.
	 * @param actual   {@link ProcedureBuildImpl}.
	 */
	private static void validateProcedureType(ProcedureTypeBuilder expected, ProcedureBuildImpl actual) {
		String resource = actual.getResource();
		Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass = actual.getProcedureSourceServiceFactory();
		String procedureName = actual.getProcedureName();
		if (serviceFactoryClass == null) {
			// Default procedures
			ProcedureLoaderUtil.validateProcedureType(expected, resource, procedureName);
		} else {
			// Configured procedures
			ProcedureLoaderUtil.validateProcedureType(expected, resource, serviceFactoryClass, procedureName);
		}
	}

	/*
	 * =========================== Tests ================================
	 */

	/**
	 * Ensure can use primitive types.
	 */
	public void testDirectPrimitives() throws Exception {
		PrimitiveTypes types = this.primitives(true, (byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		assertPrimitives(types);
	}

	protected abstract PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int,
			long _long, float _float, double _double) throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testPrimitivesType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.primitives(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		type.addObjectType(Boolean.class.getName(), Boolean.class, null);
		type.addObjectType(Byte.class.getName(), Byte.class, null);
		type.addObjectType(Short.class.getName(), Short.class, null);
		type.addObjectType(Character.class.getName(), Character.class, null);
		type.addObjectType(Integer.class.getName(), Integer.class, null);
		type.addObjectType(Long.class.getName(), Long.class, null);
		type.addObjectType(Float.class.getName(), Float.class, null);
		type.addObjectType(Double.class.getName(), Double.class, null);
		type.setNextArgumentType(PrimitiveTypes.class);
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke primitive types.
	 */
	public void testInvokePrimitives() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<String> functionName = new Closure<>();
		CompileVar<PrimitiveTypes> primitives = new CompileVar<>();
		compiler.office((context) -> {

			// Load inputs
			value(context, Boolean.TRUE);
			value(context, Byte.valueOf((byte) 1));
			value(context, Short.valueOf((short) 2));
			value(context, Character.valueOf('3'));
			value(context, Integer.valueOf(4));
			value(context, Long.valueOf(5));
			value(context, Float.valueOf(6.0f));
			value(context, Double.valueOf(7.0));

			// Capture result
			OfficeSection result = context.addSection("RESULT", PrimitiveReturn.class);
			context.variable(null, PrimitiveTypes.class, primitives);

			// Load polyglot function
			functionName.value = this.primitives(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertPrimitives(primitives.getValue());
	}

	public static class PrimitiveReturn {
		public void service(@Parameter PrimitiveTypes result, Out<PrimitiveTypes> out) {
			out.set(result);
		}
	}

	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.primitives(builder);
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		context.getOfficeArchitect().link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
				handleResult);
		return builder.getManagedFunctionName();
	}

	protected abstract void primitives(ProcedureBuilder builder);

	private static void assertPrimitives(PrimitiveTypes types) {
		assertTrue("boolean", types.isPrimitiveBoolean());
		assertEquals("byte", 1, types.getPrimitiveByte());
		assertEquals("short", 2, types.getPrimitiveShort());
		assertEquals("char", '3', types.getPrimitiveChar());
		assertEquals("integer", 4, types.getPrimitiveInt());
		assertEquals("long", 5, types.getPrimitiveLong());
		assertEquals("float", 6.0f, types.getPrimitiveFloat());
		assertEquals("double", 7.0, types.getPrimitiveDouble());
	}

	/**
	 * Ensure can pass in a Java object.
	 */
	public void testDirectObject() throws Exception {
		String string = "TEST";
		JavaObject object = new JavaObject("test");
		int[] primitiveArray = new int[] { 1 };
		JavaObject[] objectArray = new JavaObject[] { object };
		ObjectTypes types = this.objects(string, object, primitiveArray, objectArray);
		assertObjects(types, string, object, primitiveArray, objectArray);
	}

	protected abstract ObjectTypes objects(String string, JavaObject object, int[] primitiveArray,
			JavaObject[] objectArray) throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testObjectType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.objects(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		type.addObjectType(String.class.getName(), String.class, null);
		type.addObjectType(JavaObject.class.getName(), JavaObject.class, null);
		type.addObjectType(int[].class.getName(), int[].class, null);
		type.addObjectType(JavaObject[].class.getName(), JavaObject[].class, null);
		type.setNextArgumentType(ObjectTypes.class);
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke object types.
	 */
	public void testInvokeObject() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		String string = "TEST";
		JavaObject object = new JavaObject("test");
		int[] primitiveArray = new int[] { 1 };
		JavaObject[] objectArray = new JavaObject[] { object };
		CompileVar<ObjectTypes> var = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {

			// Load object
			value(context, string);
			value(context, object);
			value(context, primitiveArray);
			value(context, objectArray);

			// Capture result
			OfficeSection result = context.addSection("RESULT", ObjectReturn.class);
			context.variable(null, ObjectTypes.class, var);

			// Load polyglot function
			functionName.value = this.objects(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		ObjectTypes types = var.getValue();
		assertObjects(types, string, object, primitiveArray, objectArray);
	}

	public static class ObjectReturn {
		public void service(@Parameter ObjectTypes result, Out<ObjectTypes> out) {
			out.set(result);
		}
	}

	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.objects(builder);
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		context.getOfficeArchitect().link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
				handleResult);
		return builder.getManagedFunctionName();
	}

	protected abstract void objects(ProcedureBuilder builder);

	private static void assertObjects(ObjectTypes types, String string, JavaObject object, int[] primitiveArray,
			JavaObject[] objectArray) {
		assertEquals("string", string, types.getString());
		assertSame("object", object, types.getObject());
		assertSame("primitiveArray", primitiveArray, types.getPrimitiveArray());
		assertSame("objectArray", objectArray, types.getObjectArray());
	}

	/**
	 * Ensure can pass collections.
	 */
	public void testDirectCollections() throws Exception {
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		CollectionTypes types = this.collections(list, set, map);
		assertCollections(types, list, set, map);
	}

	protected abstract CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map)
			throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testCollectionsType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.collections(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		type.addObjectType(List.class.getName(), List.class, null);
		type.addObjectType(Set.class.getName(), Set.class, null);
		type.addObjectType(Map.class.getName(), Map.class, null);
		type.setNextArgumentType(CollectionTypes.class);
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke collections.
	 */
	public void testInvokeCollections() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		CompileVar<CollectionTypes> var = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {

			// Load collections
			value(context, list);
			value(context, set);
			value(context, map);

			// Capture result
			OfficeSection result = context.addSection("RESULT", CollectionReturn.class);
			context.variable(null, CollectionTypes.class, var);

			// Load polyglot function
			functionName.value = this.collections(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertCollections(var.getValue(), list, set, map);
	}

	public static class CollectionReturn {
		public void service(@Parameter CollectionTypes result, Out<CollectionTypes> out) {
			out.set(result);
		}
	}

	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.collections(builder);
		OfficeSection procedure = this.addProcedure(builder.getResource(), this.getProcedureSourceServiceFactoryClass(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		context.getOfficeArchitect().link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
				handleResult);
		return builder.getManagedFunctionName();
	}

	protected abstract void collections(ProcedureBuilder builder);

	private static void assertCollections(CollectionTypes types, List<Integer> list, Set<Character> set,
			Map<String, JavaObject> map) {
		assertSame("list", list, types.getList());
		assertSame("set", set, types.getSet());
		assertSame("map", map, types.getMap());
	}

	/**
	 * Ensure can handle variables.
	 */
	public void testDirectVariables() throws Exception {
		MockVar<String> in = new MockVar<>("2");
		MockVar<JavaObject> out = new MockVar<>();
		MockVar<Integer> var = new MockVar<>(3);
		VariableTypes types = this.variables('1', in, out, var);
		assertVariables(types, out.get(), var.get());
	}

	protected abstract VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var)
			throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testVariablesType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.variables(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		type.addVariableType(Character.class.getName(), Character.class.getName());
		type.addVariableType(String.class.getName());
		type.addVariableType(JavaObject.class.getName());
		type.addVariableType("qualified-" + Integer.class.getName(), Integer.class.getName());
		type.setNextArgumentType(VariableTypes.class);
		validateProcedureType(type, builder);
	}

	/**
	 * Sets up invoking the variables.
	 */
	private void setupInvokeVariables(CompileVar<JavaObject> out, CompileVar<Integer> var) throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<Character> val = new CompileVar<Character>('1');
		CompileVar<String> in = new CompileVar<>("2");
		compiler.office((context) -> {

			// Capture variables (if provided)
			context.variable(null, Character.class, val);
			context.variable(null, String.class, in);
			context.variable(null, JavaObject.class, out);
			context.variable("qualified", Integer.class, var);

			// Pass results
			OfficeSection pass = context.addSection("PASS", VariablePass.class);

			// Capture result
			OfficeSection section = context.addSection("RESULT", VariableReturn.class);

			// Load polyglot function
			this.variables(pass.getOfficeSectionOutput("use"), context, section.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	/**
	 * Ensure can using variables.
	 */
	public void testInvokeVariables() throws Throwable {

		// Setup invoking variables
		CompileVar<JavaObject> out = new CompileVar<>();
		CompileVar<Integer> var = new CompileVar<>(3);
		this.setupInvokeVariables(out, var);

		// Invoke the function
		Closure<VariableTypes> capture = new Closure<>();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "PASS.service", capture);
		assertVariables(capture.value, out.getValue(), var.getValue());
	}

	public static class VariablePass {
		@Next("use")
		public void service(@Parameter Closure<VariableTypes> capture, Out<Closure<VariableTypes>> out) {
			out.set(capture);
		}
	}

	public static class VariableReturn {
		public void service(@Parameter VariableTypes result, In<Closure<VariableTypes>> in) {
			in.get().value = result;
		}
	}

	protected void variables(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.variables(builder);
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		office.link(pass, procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		office.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), handleResult);
	}

	protected abstract void variables(ProcedureBuilder builder);

	private static void assertVariables(VariableTypes types, JavaObject out, Integer var) {
		assertVariables(types);
		assertNotNull("No out", out);
		assertEquals("update out", "test", out.getIdentifier());
		assertNotNull("no var", var);
		assertEquals("update var", Integer.valueOf(4), var);
	}

	private static void assertVariables(VariableTypes types) {
		assertEquals("val", '1', types.getVal());
		assertEquals("in", "2", types.getIn());
		assertEquals("var", 3, types.getVar());
	}

	/**
	 * Ensure can provide {@link Parameter}.
	 */
	public void testDirectParameter() throws Exception {
		ParameterTypes types = this.parameter("test");
		assertParameter(types);
	}

	protected abstract ParameterTypes parameter(String parameter) throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testParameterType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.parameter(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(),
				String.class);
		type.setNextArgumentType(ParameterTypes.class);
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can use parameter.
	 */
	public void testInvokeParameter() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<ParameterTypes> result = new CompileVar<>();
		compiler.office((context) -> {

			// Pass parameter
			OfficeSection passSection = context.addSection("PASS", ParameterPass.class);

			// Capture result
			OfficeSection resultSection = context.addSection("RESULT", ParameterReturn.class);
			context.variable(null, ParameterTypes.class, result);

			// Load polyglot function
			this.parameter(passSection.getOfficeSectionOutput("use"), context,
					resultSection.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "PASS.service", null);
		assertParameter(result.getValue());
	}

	public static class ParameterPass {
		@Next("use")
		public String service() {
			return "test";
		}
	}

	public static class ParameterReturn {
		public void service(@Parameter ParameterTypes result, Out<ParameterTypes> out) {
			out.set(result);
		}
	}

	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.parameter(builder);
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		office.link(pass, procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		office.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), handleResult);
	}

	protected abstract void parameter(ProcedureBuilder builder);

	private static void assertParameter(ParameterTypes types) {
		assertEquals("parameter", "test", types.getParameter());
	}

	/**
	 * Ensure safe to run directly multi-threaded.
	 */
	@StressTest
	public void testDirectMultiThreaded() throws Exception {
		this.doMultiThreadedTest(10, 10000, 60, () -> this.testDirectVariables());
	}

	/**
	 * Ensure safe to run invoked multi-threaded.
	 */
	@StressTest
	public void testInvokeMultiThreaded() throws Throwable {

		// Setup
		CompileVar<JavaObject> out = new CompileVar<>();
		CompileVar<Integer> var = new CompileVar<>(3);
		this.setupInvokeVariables(out, var);

		// Undertake test
		this.doMultiThreadedTest(10, 10000, 60, () -> {
			Closure<VariableTypes> capture = new Closure<>();
			CompileOfficeFloor.invokeProcess(this.officeFloor, "PASS.service", capture);
			assertVariables(capture.value);
		});
	}

	/**
	 * Ensure can run direct for web.
	 */
	public void testDirectWeb() throws Exception {
		MockObjectResponse<WebTypes> types = new MockObjectResponse<>();
		this.web("path", "query", "header", "cookie", new MockHttpParameters("test"), new MockHttpObject(1, "test"),
				types);
		assertWeb(types.getObject());
	}

	protected abstract void web(String pathParameter, String queryParameter, String headerParameter,
			String cookieParameter, MockHttpParameters httpParameters, MockHttpObject httpObject,
			ObjectResponse<WebTypes> response) throws Exception;

	/**
	 * Ensure can invoke for web.
	 */
	public void testInvokeWeb() throws Throwable {
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.mockHttpServer((mockServer) -> server.value = mockServer);
		compiler.web((context) -> {
			HttpUrlContinuation input = context.getWebArchitect().getHttpInput(false, "/test/{param}");
			this.web(input.getInput(), context);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		MockHttpResponse response = server.value
				.send(MockHttpServer.mockRequest("/test/path?param=query").header("param", "header")
						.cookie("param", "cookie").header("mock", "test").header("content-type", "application/json")
						.entity(mapper.writeValueAsString(new MockHttpObject(1, "test"))));
		assertEquals("Incorrect status", 200, response.getStatus().getStatusCode());
		WebTypes types = mapper.readValue(response.getEntity(), WebTypes.class);
		assertWeb(types);
	}

	protected void web(OfficeFlowSourceNode pass, CompileWebContext context) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.web(builder);
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), false, builder.getProperties(), context);
		context.getOfficeArchitect().link(pass, procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
	}

	protected abstract void web(ProcedureBuilder builder);

	private static void assertWeb(WebTypes types) {
		assertNotNull("Missing types", types);
		assertEquals("path", types.getPathParameter());
		assertEquals("query", types.getQueryParameter());
		assertEquals("header", types.getHeaderParameter());
		assertEquals("cookie", types.getCookieParameter());
		assertNotNull("Missing HTTP Parameters", types.getHttpParameters());
		assertEquals("HTTP Parameters value", "test", types.getHttpParameters().getMock());
		assertNotNull("Missing HTTP Object", types.getHttpObject());
		assertEquals("HTTP Object identifier", 1, types.getHttpObject().getIdentifier());
		assertEquals("HTTP Object message", "test", types.getHttpObject().getMessage());
		assertNotNull("Missing object", types.getObject());
		assertEquals("object", "path", types.getObject().getIdentifier());
	}

	/**
	 * Ensure can run direct for {@link HttpException}.
	 */
	public void testDirectHttpException() throws Throwable {
		try {
			this.httpException();
			fail("Should not be successful");
		} catch (HttpException ex) {
			HttpException expected = new HttpException(422, "test");
			assertEquals("status", expected.getHttpStatus().getStatusCode(), ex.getHttpStatus().getStatusCode());
			Class<?> causeClass = ex.getCause() != null ? ex.getCause().getClass() : null;
			assertEquals("cause class: " + causeClass, expected.getCause().getClass(), causeClass);
			String causeMessage = ex.getCause() != null ? ex.getCause().getMessage() : null;
			assertEquals("cause message: " + causeMessage, expected.getCause().getMessage(), causeMessage);
		}
	}

	protected abstract void httpException() throws Throwable;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testHttpExceptionType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.httpException(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		if (this.isSupportExceptions()) {
			type.addEscalationType(HttpException.class.getName(), HttpException.class);
		}
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke to handle {@link HttpException}.
	 */
	public void testInvokeHttpException() throws Throwable {
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.mockHttpServer((mockServer) -> server.value = mockServer);
		compiler.web((context) -> {
			HttpUrlContinuation input = context.getWebArchitect().getHttpInput(false, "/error");
			this.httpException(input.getInput(), context);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/error"));
		response.assertResponse(422, "{\"error\":\"test\"}");
	}

	protected void httpException(OfficeFlowSourceNode pass, CompileWebContext context) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.httpException(builder);
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), false, builder.getProperties(), context);
		context.getOfficeArchitect().link(pass, procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
	}

	protected abstract void httpException(ProcedureBuilder builder);

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testFlowType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.flow(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(),
				String.class);
		type.addFlowType("flow", null);
		type.addFlowType("flowWithCallback", null);
		type.addFlowType("flowWithParameter", String.class);
		type.addFlowType("flowWithParameterAndCallback", String.class);
		if (this.isSupportExceptions()) {
			type.addEscalationType(IOException.class.getName(), IOException.class);
		}
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke flow.
	 */
	public void testFlow() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<String> result = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {
			context.variable(null, String.class, result);
			OfficeSection section = context.addSection("HANDLERS", FlowHandlers.class);
			functionName.value = this.flow(context, section.getOfficeSectionInput("nextFunction"),
					section.getOfficeSectionInput("flow"), section.getOfficeSectionInput("flowWithCallback"),
					section.getOfficeSectionInput("flowWithParameterAndCallback"),
					section.getOfficeSectionInput("flowWithParameter"), section.getOfficeSectionInput("exception"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Test next function
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, "nextFunction");
		assertEquals("nextFunction", result.getValue());

		// Test trigger flow
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, "flow");
		assertEquals("flow", result.getValue());

		// Test flow callbacks with parameters
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, "callbacks");
		assertEquals("flowWithCallback-flowWithParameterAndCallback-1-flowWithParameter-2", result.getValue());

		// Test exception
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, "exception");
		assertEquals("exception", result.getValue());
	}

	public static class FlowHandlers {
		public void nextFunction(Out<String> result) {
			result.set("nextFunction");
		}

		public void flow(Out<String> result) {
			result.set("flow");
		}

		public void flowWithCallback(Out<String> result) {
			result.set("flowWithCallback");
		}

		public void flowWithParameterAndCallback(@Parameter String param, Var<String> result) {
			result.set(result.get() + "-flowWithParameterAndCallback-" + param);
		}

		public void flowWithParameter(@Parameter String param, Var<String> result) {
			result.set(result.get() + "-flowWithParameter-" + param);
		}

		public void exception(Out<String> result) {
			result.set("exception");
		}
	}

	protected String flow(CompileOfficeContext context, OfficeSectionInput next, OfficeSectionInput flow,
			OfficeSectionInput flowWithCallback, OfficeSectionInput flowWithParameterAndCallback,
			OfficeSectionInput flowWithParameter, OfficeSectionInput exception) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.flow(builder);
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection procedure = this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(),
				builder.getProcedureName(), true, builder.getProperties(), context);
		office.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), next);
		office.link(procedure.getOfficeSectionOutput("flow"), flow);
		office.link(procedure.getOfficeSectionOutput("flowWithCallback"), flowWithCallback);
		office.link(procedure.getOfficeSectionOutput("flowWithParameterAndCallback"), flowWithParameterAndCallback);
		office.link(procedure.getOfficeSectionOutput("flowWithParameter"), flowWithParameter);
		if (this.isSupportExceptions()) {
			office.link(procedure.getOfficeSectionOutput(IOException.class.getName()), exception);
		} else {
			OfficeEscalation escalation = office.addOfficeEscalation(IOException.class.getName());
			office.link(escalation, exception);
		}
		return builder.getManagedFunctionName();
	}

	protected abstract void flow(ProcedureBuilder builder);

	/**
	 * Validate direct {@link AsynchronousFlow}.
	 */
	public void testDirectAsynchronousFlow() throws Throwable {
		MockAsynchronousFlow flowOne = new MockAsynchronousFlow();
		MockAsynchronousFlow flowTwo = new MockAsynchronousFlow();
		this.asynchronousFlow(flowOne, flowTwo);
		assertTrue("Flow one should be complete", flowOne.isComplete());
		assertFalse("Flow two not yet complete", flowTwo.isComplete());
		flowOne.getCompletion().run();
		assertTrue("Flow two should now be complete", flowTwo.isComplete());
		assertNull("Should not have flow two completion", flowTwo.getCompletion());
	}

	protected abstract void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) throws Exception;

	/**
	 * Ensure correct {@link ProcedureType}.
	 */
	public void testAsynchronousFlowType() {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.asynchronousFlow(builder);
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder(builder.getProcedureName(), null);
		validateProcedureType(type, builder);
	}

	/**
	 * Ensure can invoke {@link AsynchronousFlow}.
	 */
	public void testInvokeAsynchronousFlow() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {
			functionName.value = this.asynchronousFlow(context);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
	}

	protected String asynchronousFlow(CompileOfficeContext context) {
		ProcedureBuildImpl builder = new ProcedureBuildImpl();
		this.asynchronousFlow(builder);
		this.addProcedure(builder.getResource(), builder.getProcedureSourceServiceFactory(), builder.getProcedureName(),
				false, builder.getProperties(), context);
		return builder.getManagedFunctionName();
	}

	protected abstract void asynchronousFlow(ProcedureBuilder builder);

	/**
	 * Loads a value.
	 *
	 * @param context {@link CompileOfficeContext}.
	 * @param value   Value.
	 */
	private static void value(CompileOfficeContext context, Object value) {
		Singleton.load(context.getOfficeArchitect(), value);
	}

}
