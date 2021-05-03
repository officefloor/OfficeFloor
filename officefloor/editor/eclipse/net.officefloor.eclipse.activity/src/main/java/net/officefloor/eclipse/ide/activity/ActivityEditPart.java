/*-
 * #%L
 * [bundle] Activity Eclipse IDE
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
