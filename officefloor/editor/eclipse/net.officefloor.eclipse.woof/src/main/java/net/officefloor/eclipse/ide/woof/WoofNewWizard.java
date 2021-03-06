/*-
 * #%L
 * WoOF Eclipse IDE
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
