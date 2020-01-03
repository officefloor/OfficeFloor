package net.officefloor.eclipse.ide.section;

import org.eclipse.ui.INewWizard;

import net.officefloor.eclipse.ide.newwizard.AbstractNewWizard;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.section.SectionEditor;
import net.officefloor.model.section.SectionModel;

/**
 * {@link SectionEditor} {@link INewWizard}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNewWizard extends AbstractNewWizard<SectionModel> {

	@Override
	protected AbstractAdaptedIdeEditor<SectionModel, ?, ?> createEditor(EnvironmentBridge envBridge) {
		return new SectionEditor(envBridge);
	}

}