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