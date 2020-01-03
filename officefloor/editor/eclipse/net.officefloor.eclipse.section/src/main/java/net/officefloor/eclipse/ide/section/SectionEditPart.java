package net.officefloor.eclipse.ide.section;

import net.officefloor.eclipse.ide.AbstractAdaptedEditorPart;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.section.SectionEditor;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Section Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditPart extends AbstractAdaptedEditorPart<SectionModel, SectionEvent, SectionChanges> {

	@Override
	public AbstractAdaptedIdeEditor<SectionModel, SectionEvent, SectionChanges> createEditor(
			EnvironmentBridge envBridge) {
		return new SectionEditor(envBridge);
	}

}