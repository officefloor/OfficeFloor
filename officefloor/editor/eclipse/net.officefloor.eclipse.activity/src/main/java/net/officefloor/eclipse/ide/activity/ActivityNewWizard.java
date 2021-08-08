/*-
 * #%L
 * [bundle] Activity Eclipse IDE
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
