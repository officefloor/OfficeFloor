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
package net.officefloor.eclipse.skin.woof;

import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.woof.WoofSectionInputModel;

/**
 * {@link OfficeFloorFigure} for the {@link WoofSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInputFigure extends OfficeFloorFigure {

	/**
	 * Specifies the section input name to display.
	 * 
	 * @param sectionInputName
	 *            Section input name to display.
	 */
	void setSectionInputName(String sectionInputName);

	/**
	 * Specifies the URI to be displayed.
	 * 
	 * @param uri
	 *            URI to be displayed.
	 */
	void setUri(String uri);

	/**
	 * Obtains the {@link IFigure} for the URI.
	 * 
	 * @return {@link IFigure} for the URI.
	 */
	IFigure getUriFigure();

}