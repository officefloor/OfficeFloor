/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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
