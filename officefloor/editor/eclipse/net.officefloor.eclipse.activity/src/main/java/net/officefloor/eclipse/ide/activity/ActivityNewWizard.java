package net.officefloor.eclipse.ide.activity;

import org.eclipse.ui.INewWizard;

import net.officefloor.activity.model.ActivityModel;
import net.officefloor.eclipse.ide.newwizard.AbstractNewWizard;
import net.officefloor.gef.activity.ActivityEditor;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;

/**
 * {@link ActivityEditor} {@link INewWizard}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityNewWizard extends AbstractNewWizard<ActivityModel> {

	@Override
	protected AbstractAdaptedIdeEditor<ActivityModel, ?, ?> createEditor(EnvironmentBridge envBridge) {
		return new ActivityEditor(envBridge);
	}

}