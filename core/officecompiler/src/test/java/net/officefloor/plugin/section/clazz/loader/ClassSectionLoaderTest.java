/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.plugin.section.clazz.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;

/**
 * Tests {@link ClassSectionLoader} in both all-methods and single-method modes.
 */
public class ClassSectionLoaderTest {

	@Test
	public void allMethodsModeNamesSectionObjectsByType() throws Exception {
		SectionType sectionType = SectionLoaderUtil.loadSectionType(AllMethodsSectionSource.class, "");

		SectionObjectType[] objects = sectionType.getSectionObjectTypes();
		assertEquals(1, objects.length,
				"Two functions sharing Connection type should deduplicate to one section object");
		assertEquals(Connection.class.getName(), objects[0].getSectionObjectName(),
				"Section object should be named by type in all-methods mode");
		assertEquals(Connection.class.getName(), objects[0].getObjectType());
	}

	public static class AllMethodsSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			ClassSectionLoader loader = new ClassSectionLoader(designer, context);
			loader.addManagedFunctions("NAMESPACE", TwoFunctionManagedFunctionSource.class.getName(),
					context.createPropertyList(), null);
			designer.link(designer.addSectionInput("funcA", null), loader.getFunction("funcA").getFunction());
			designer.link(designer.addSectionInput("funcB", null), loader.getFunction("funcB").getFunction());
			loader.load();
		}
	}

	public static class TwoFunctionManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder builder, ManagedFunctionSourceContext context)
				throws Exception {
			builder.addManagedFunctionType("funcA", Indexed.class, None.class)
					.setFunctionFactory(NoOpFunction.INSTANCE)
					.addObject(Connection.class).setLabel("CONN_A");

			builder.addManagedFunctionType("funcB", Indexed.class, None.class)
					.setFunctionFactory(NoOpFunction.INSTANCE)
					.addObject(Connection.class).setLabel("CONN_B");
		}
	}

	@Test
	public void singleMethodModeNamesSectionObjectsByLabel() throws Exception {
		SectionType sectionType = SectionLoaderUtil.loadSectionType(SingleMethodSectionSource.class, "");

		// Only the named function is loaded — the other function in the source is filtered out
		assertEquals(1, sectionType.getSectionInputTypes().length,
				"Only the specified method should be loaded");
		assertEquals("procedure", sectionType.getSectionInputTypes()[0].getSectionInputName());

		// Objects of the loaded function are named by label, not by type
		Map<String, SectionObjectType> byName = Arrays.stream(sectionType.getSectionObjectTypes())
				.collect(Collectors.toMap(SectionObjectType::getSectionObjectName, o -> o));

		assertEquals(2, byName.size(), "Should have two section objects, one per label");

		SectionObjectType conn = byName.get("CONN");
		assertNotNull(conn, "Should have section object named CONN");
		assertEquals(Connection.class.getName(), conn.getObjectType());

		SectionObjectType str = byName.get("STR");
		assertNotNull(str, "Should have section object named STR");
		assertEquals(String.class.getName(), str.getObjectType());
	}

	public static class SingleMethodSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			ClassSectionLoader loader = new ClassSectionLoader(designer, context);
			loader.setSingleMethod("procedure");
			loader.addManagedFunctions("procedure", LabeledFunctionManagedFunctionSource.class.getName(),
					context.createPropertyList(), null);
			designer.link(designer.addSectionInput("procedure", null),
					loader.getFunction("procedure").getFunction());
			loader.load();
		}
	}

	/**
	 * Two functions in the same namespace: {@code procedure} (the target) and
	 * {@code other} (should be filtered out in single-method mode).
	 */
	public static class LabeledFunctionManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder builder, ManagedFunctionSourceContext context)
				throws Exception {
			ManagedFunctionTypeBuilder<Indexed, None> procedure = builder.addManagedFunctionType("procedure",
					Indexed.class, None.class).setFunctionFactory(NoOpFunction.INSTANCE);
			procedure.addObject(Connection.class).setLabel("CONN");
			procedure.addObject(String.class).setLabel("STR");

			// This function must not appear in the loaded section
			builder.addManagedFunctionType("other", Indexed.class, None.class)
					.setFunctionFactory(NoOpFunction.INSTANCE)
					.addObject(Connection.class).setLabel("OTHER_CONN");
		}
	}

	private static class NoOpFunction extends StaticManagedFunction<Indexed, None> {

		static final NoOpFunction INSTANCE = new NoOpFunction();

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {
		}
	}

}
