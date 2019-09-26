/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.gef.woof.test;

import java.io.IOException;

import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.AbstractIdeTestApplication;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.woof.WoofEditor;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpInputModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSecurityContentTypeModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateLinkModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateRenderHttpMethodModel;

/**
 * Tests the {@link WoofEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofIdeTestApplication extends AbstractIdeTestApplication<WoofModel, WoofEvent, WoofChanges> {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	protected AbstractAdaptedIdeEditor<WoofModel, WoofEvent, WoofChanges> createEditor(EnvironmentBridge envBridge) {
		return new WoofEditor(envBridge);
	}

	@Override
	protected String getConfigurationFileName() {
		return "Test.woof.xml";
	}

	@Override
	protected String getReplaceConfigurationFileName() {
		return "Replace.woof.xml";
	}

	@Override
	public void init() throws Exception {
		this.register(WoofExceptionModel.class, (model) -> {
			model.setClassName(IOException.class.getName());
		});
		this.register(WoofGovernanceModel.class, (model) -> {
			model.setWoofGovernanceName("Governance");
			model.setGovernanceSourceClassName(ClassGovernanceSource.class.getName());
			model.addProperty(
					new PropertyModel(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName()));
		});
		this.register(WoofHttpContinuationModel.class, (model) -> {
			model.setApplicationPath("/path");
			model.setIsSecure(true);
		});
		this.register(WoofHttpInputModel.class, (model) -> {
			model.setHttpMethod("POST");
			model.setApplicationPath("/path");
			model.setIsSecure(true);
		});
		this.register(WoofResourceModel.class, (model) -> {
			model.setResourcePath("/resource");
		});
		this.register(WoofSectionModel.class, (model) -> {
			model.setWoofSectionName("Section");
			model.setSectionSourceClassName(ClassSectionSource.class.getName());
			model.setSectionLocation(MockSection.class.getName());
		});
		this.register(WoofSecurityModel.class, (model) -> {
			model.setHttpSecurityName("Security");
			model.setHttpSecuritySourceClassName(BasicHttpSecuritySource.class.getName());
			model.setTimeout(1000);
			model.addContentType(new WoofSecurityContentTypeModel("application/json"));
			model.addContentType(new WoofSecurityContentTypeModel("application/xml"));
		});
		this.register(WoofTemplateModel.class, (model) -> {
			model.setApplicationPath("/path");
			model.setIsTemplateSecure(true);
			model.addLink(new WoofTemplateLinkModel("link", false));
			model.addLink(new WoofTemplateLinkModel("secure", true));
			model.setTemplateLocation("net/officefloor/gef/woof/test/Template.html");
			model.setTemplateClassName(MockLogic.class.getName());
			model.setRedirectValuesFunction("redirect");
			model.setTemplateContentType("application/text");
			model.setTemplateCharset("UTF-8");
			model.setLinkSeparatorCharacter("+");
			model.addRenderHttpMethod(new WoofTemplateRenderHttpMethodModel("POST"));
			model.addRenderHttpMethod(new WoofTemplateRenderHttpMethodModel("PUT"));
		});
	}

	/**
	 * Mock section {@link Class} for testing.
	 */
	public static class MockSection {

		@FlowInterface
		public static interface Flows {
			void flow();
		}

		public void input(Flows flows) {
		}
	}

	/**
	 * Mock logic class for testing.
	 */
	public static class MockLogic {

		@FlowInterface
		public static interface Flows {
			void flow();
		}

		public MockLogic redirect() {
			return this;
		}

		public void getTemplate(Flows flows) {
		}
	}

}