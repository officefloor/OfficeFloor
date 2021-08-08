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

package net.officefloor.web.template.type;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.CompileContextImpl;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.compile.test.util.LoaderUtil;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Utility class for testing the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLoaderUtil {

	/**
	 * Creates the {@link SectionDesigner} to create the expected type.
	 * 
	 * @return {@link SectionDesigner}.
	 */
	public static SectionDesigner createSectionDesigner() {
		OfficeFloorCompiler compiler = getOfficeFloorCompiler();
		NodeContext context = (NodeContext) compiler;
		OfficeNode office = context.createOfficeNode("<office>", null);
		return context.createSectionNode(SectionLoaderUtil.class.getSimpleName(), office);
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the OfficeFloor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * Validates the {@link WebTemplateType}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} containing the expected type.
	 * @param actualType
	 *            Actual {@link WebTemplateType} to validate.
	 */
	public static void validateWebTemplateType(SectionDesigner designer, WebTemplateType actualType) {

		// Compile Context
		CompileContext compileContext = new CompileContextImpl(null);

		// Cast to obtain expected section type
		if (!(designer instanceof SectionNode)) {
			Assert.fail("designer must be created from createSectionDesigner");
		}
		SectionType expectedSection = ((SectionNode) designer).loadSectionType(compileContext);

		// Validate the section outputs are as expected
		SectionOutputType[] eOutputs = expectedSection.getSectionOutputTypes();
		WebTemplateOutputType[] aOutputs = actualType.getWebTemplateOutputTypes();
		LoaderUtil.assertLength("Incorrect number of outputs", eOutputs, (output) -> output.getSectionOutputName(),
				aOutputs, (output) -> output.getWebTemplateOutputName());
		for (int i = 0; i < eOutputs.length; i++) {
			SectionOutputType eOutput = eOutputs[i];
			WebTemplateOutputType aOutput = aOutputs[i];
			Assert.assertEquals("Incorrect name for output " + i, eOutput.getSectionOutputName(),
					aOutput.getWebTemplateOutputName());
			Assert.assertEquals("Incorrect argument type for output " + i, eOutput.getArgumentType(),
					aOutput.getArgumentType());
			Assert.assertEquals("Incorrect escalation only for output " + i, eOutput.isEscalationOnly(),
					aOutput.isEscalationOnly());
		}
	}

}
