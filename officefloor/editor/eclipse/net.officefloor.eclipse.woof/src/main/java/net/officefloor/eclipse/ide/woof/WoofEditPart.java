/*-
 * #%L
 * [bundle] WoOF Eclipse IDE
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

package net.officefloor.eclipse.ide.woof;

import net.officefloor.eclipse.ide.AbstractAdaptedEditorPart;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.woof.WoofEditor;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;

/**
 * Web on OfficeFloor (WoOF) Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditPart extends AbstractAdaptedEditorPart<WoofModel, WoofEvent, WoofChanges> {

	@Override
	public AbstractAdaptedIdeEditor<WoofModel, WoofEvent, WoofChanges> createEditor(EnvironmentBridge envBridge) {
		return new WoofEditor(envBridge);
	}

}
