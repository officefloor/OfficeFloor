/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.wizard.file;

import net.officefloor.model.section.SectionModel;

import org.eclipse.jface.wizard.IWizard;

/**
 * {@link IWizard} to create a {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNewWizard extends AbstractNewWizard {

	/**
	 * Initiate.
	 */
	public SectionNewWizard() {
		super("New Section", "Create a new Section", "new_section", "section",
				"<section/>");
	}

}