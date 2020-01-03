package net.officefloor.eclipse.ide;

import org.eclipse.gef.mvc.fx.ui.parts.FXEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

import net.officefloor.gef.editor.AdaptedEditorModule;

/**
 * {@link EditorActionBarContributor} for the {@link AdaptedEditorModule}
 * {@link IEditorPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class IdeEditorActionBarContributor extends FXEditorActionBarContributor {

	@Override
	public void setActiveEditor(IEditorPart activeEditor) {
		super.setActiveEditor(activeEditor);

		// Provide save action
		this.getActionBars().setGlobalActionHandler(ActionFactory.SAVE.getId(),
				ActionFactory.SAVE.create(activeEditor.getSite().getWorkbenchWindow()));

		// Provide save as action
		this.getActionBars().setGlobalActionHandler(ActionFactory.SAVE_AS.getId(),
				ActionFactory.SAVE_AS.create(activeEditor.getSite().getWorkbenchWindow()));
	}

}