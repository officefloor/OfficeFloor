package net.officefloor.eclipse.ide.activity;

import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.eclipse.ide.AbstractAdaptedEditorPart;
import net.officefloor.gef.activity.ActivityEditor;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;

/**
 * Activity Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityEditPart extends AbstractAdaptedEditorPart<ActivityModel, ActivityEvent, ActivityChanges> {

	@Override
	public AbstractAdaptedIdeEditor<ActivityModel, ActivityEvent, ActivityChanges> createEditor(EnvironmentBridge envBridge) {
		return new ActivityEditor(envBridge);
	}

}