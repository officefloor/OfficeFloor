/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.build;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Consumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.extension.WebTemplateExtensionContext;
import net.officefloor.web.template.section.WebTemplateLinkAnnotation;
import net.officefloor.web.template.type.WebTemplateLoader;
import net.officefloor.web.template.type.WebTemplateLoaderUtil;
import net.officefloor.web.template.type.WebTemplateType;

/**
 * Tests the {@link WebTemplateLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load with simple {@link WebTemplate}.
	 */
	public void testSimple() {
		this.doTypeTest(false, "/path", "template", null, null);
	}

	/**
	 * Ensure provides link.
	 */
	public void testTemplateLink() {
		this.doTypeTest(false, "/path", "#{link}", null, (designer) -> {
			designer.addSectionInput("link", null).addAnnotation(new WebTemplateLinkAnnotation(false, "link"));
			designer.addSectionOutput("link", null, false);
		});
	}

	/**
	 * Ensure no output if link handled by method.
	 */
	public void testHandledLink() {
		this.doTypeTest(false, "/path", "#{link}", (template) -> {
			template.setLogicClass(HandledLink.class.getName());
		}, (designer) -> {
			designer.addSectionInput("link", null).addAnnotation(new WebTemplateLinkAnnotation(false, "link"));
		});
	}

	public static class HandledLink {
		public void link() {
		}
	}

	/**
	 * Ensure provides output from logic.
	 */
	public void testLogicOutput() {
		this.doTypeTest(false, "/path", "template", (template) -> {
			template.setLogicClass(LogicOutput.class.getName());
		}, (designer) -> {
			designer.addSectionInput("getTemplate", null);
			designer.addSectionOutput("flow", null, false);
		});
	}

	@FlowInterface
	public static interface LogicOutputFlows {
		void flow();
	}

	public static class LogicOutput {
		public void getTemplate(LogicOutputFlows flows) {
		}
	}

	/**
	 * Ensure handle dynamic path.
	 */
	public void testDynamicPath() {
		this.doTypeTest(false, "/path/{param}", "template", (template) -> {
			template.setLogicClass(DynamicPath.class.getName());
			template.setRedirectValuesFunction("redirect");
		}, (designer) -> {
			designer.addSectionInput("getParam", null);
			designer.addSectionInput("redirect", null);
		});
	}

	public static class DynamicPath {
		public DynamicPath redirect() {
			return this;
		}

		public String getParam() {
			return "value";
		}
	}

	/**
	 * Ensure can extend the {@link WebTemplate}.
	 */
	public void testExtendTemplate() throws Exception {
		this.doTypeTest(false, "/extend", "#{override}", (template) -> {
			template.addExtension(MockWebTemplateExtension.class.getName()).addProperty("test", "available");
		}, (designer) -> {
			designer.addSectionInput("extend", null).addAnnotation(new WebTemplateLinkAnnotation(false, "extend"));
			designer.addSectionOutput("extend", null, false);
		});
	}

	public static class MockWebTemplateExtension implements WebTemplateExtension {
		@Override
		public void extendWebTemplate(WebTemplateExtensionContext context) throws Exception {
			assertEquals("Should obtain configured property", "available", context.getProperty("test"));
			context.setTemplateContent("#{extend}");
		}
	}

	/**
	 * Ensure handle template inheritance.
	 */
	public void testTemplateInheritance() throws Exception {

		// Create the loader
		WebTemplateLoader loader = WebTemplateArchitectEmployer
				.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

		// Create the template
		WebTemplate template = loader.addTemplate(false, "/path", new StringReader("<!-- {:template} --> #{link}"));

		// Provide parent
		template.setSuperTemplate(loader.addTemplate(false, "/parent", new StringReader("parent")));

		// Load the type
		WebTemplateType type = loader.loadWebTemplateType(template);

		// Create the expected type (already supplying common)
		SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
		expected.addSectionInput("link", null).addAnnotation(new WebTemplateLinkAnnotation(false, "link"));
		expected.addSectionInput("renderTemplate", null);
		expected.addSectionOutput("link", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);
		expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());

		// Validate the type
		WebTemplateLoaderUtil.validateWebTemplateType(expected, type);
	}

	/**
	 * Ensure inherit link from parent template.
	 */
	public void testTemplateInheritLink() throws Exception {

		// Create the loader
		WebTemplateLoader loader = WebTemplateArchitectEmployer
				.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

		// Create the template
		WebTemplate template = loader.addTemplate(false, "/path", new StringReader("<!-- {:section} --> override"));

		// Provide parent
		template.setSuperTemplate(
				loader.addTemplate(false, "/parent", new StringReader("#{link} <!-- {section}--> overridden")));

		// Load the type
		WebTemplateType type = loader.loadWebTemplateType(template);

		// Create the expected type (already supplying common)
		SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
		expected.addSectionInput("link", null).addAnnotation(new WebTemplateLinkAnnotation(false, "link"));
		expected.addSectionInput("renderTemplate", null);
		expected.addSectionOutput("link", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);
		expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());

		// Validate the type
		WebTemplateLoaderUtil.validateWebTemplateType(expected, type);
	}

	/**
	 * Inheritance configuration is only for template content. Logic inheritance
	 * follows normal java class inheritance (hence extend parent template logic
	 * class to inherit logic).
	 */
	public void testTemplateNotInheritFlow() throws Exception {

		// Create the loader
		WebTemplateLoader loader = WebTemplateArchitectEmployer
				.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

		// Create the template
		WebTemplate template = loader.addTemplate(false, "/path", new StringReader("<!-- {:template} -->override"));
		template.setLogicClass(TemplateOnlyFlow.class.getName());

		// Provide parent
		WebTemplate parent = loader.addTemplate(false, "/parent", new StringReader("parent"));
		parent.setLogicClass(IgnoreParentFlowsAsUseInheritance.class.getName());
		template.setSuperTemplate(parent);

		// Load the type
		WebTemplateType type = loader.loadWebTemplateType(template);

		// Create the expected type (already supplying common)
		SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
		expected.addSectionOutput("flow", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);
		expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());

		// Validate the type
		WebTemplateLoaderUtil.validateWebTemplateType(expected, type);
	}

	public static class IgnoreParentFlowsAsUseInheritance {

		@FlowInterface
		public static interface ParentFlows {
			void parentFlow();
		}

		public void getTemplateData(ParentFlows flows) {
		}
	}

	public static class TemplateOnlyFlow {

		@FlowInterface
		public static interface Flows {
			void flow();
		}

		public void getTemplateData(Flows flows) {
		}
	}

	/**
	 * Undertakes the type test.
	 * 
	 * @param isSecure
	 *            Indicates if secure.
	 * @param path
	 *            Application Path.
	 * @param templateContent
	 *            Content for the {@link WebTemplate}.
	 * @param webTemplateDecorator
	 *            {@link WebTemplate} decorator. May be <code>null</code>.
	 * @param typeDecorator
	 *            Type decorator. May be <code>null</code>.
	 */
	public void doTypeTest(boolean isSecure, String path, String templateContent,
			Consumer<WebTemplate> webTemplateDecorator, Consumer<SectionDesigner> typeDecorator) {
		try {

			// Create the loader
			WebTemplateLoader loader = WebTemplateArchitectEmployer
					.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

			// Load the type
			WebTemplate template = loader.addTemplate(isSecure, path, new StringReader(templateContent));
			if (webTemplateDecorator != null) {
				webTemplateDecorator.accept(template);
			}
			WebTemplateType type = loader.loadWebTemplateType(template);

			// Create the expected type (already supplying common)
			SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
			if (typeDecorator != null) {
				typeDecorator.accept(expected);
			}
			expected.addSectionInput("renderTemplate", null);
			expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);
			expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());

			// Validate the type
			WebTemplateLoaderUtil.validateWebTemplateType(expected, type);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}
