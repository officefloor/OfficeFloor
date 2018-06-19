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
package net.officefloor.eclipse.skin;

import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.section.SectionFigureFactory;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.section.SectionModel;

/**
 * Skin that creates the necessary {@link IFigure} instances for representing
 * the Office Floor within Eclipse.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSkin {

	/**
	 * Obtains the {@link OfficeFigureFactory} to create necessary
	 * {@link IFigure} instances for the {@link OfficeModel}.
	 * 
	 * @return {@link OfficeFigureFactory}.
	 */
	OfficeFigureFactory getOfficeFigureFactory();

	/**
	 * Obtains the {@link OfficeFloorFigureFactory} to create necessary
	 * {@link IFigure} instances for the {@link OfficeFloorModel}.
	 * 
	 * @return {@link OfficeFloorFigureFactory}.
	 */
	OfficeFloorFigureFactory getOfficeFloorFigureFactory();

	/**
	 * Obtains the {@link SectionFigureFactory} to create necessary
	 * {@link IFigure} instances for the {@link SectionModel}.
	 * 
	 * @return {@link SectionFigureFactory}.
	 */
	SectionFigureFactory getSectionFigureFactory();

}