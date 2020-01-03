package net.officefloor.eclipse.ide.woof;

import org.eclipse.ui.INewWizard;

import net.officefloor.eclipse.ide.newwizard.AbstractNewWizard;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.woof.WoofEditor;
import net.officefloor.woof.model.woof.WoofModel;

/**
 * {@link WoofEditor} {@link INewWizard}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofNewWizard extends AbstractNewWizard<WoofModel> {

	@Override
	protected AbstractAdaptedIdeEditor<WoofModel, ?, ?> createEditor(EnvironmentBridge envBridge) {
		return new WoofEditor(envBridge);
	}

}