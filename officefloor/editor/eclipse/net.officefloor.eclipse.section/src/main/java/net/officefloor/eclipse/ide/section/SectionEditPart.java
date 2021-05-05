/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
