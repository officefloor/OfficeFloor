/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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