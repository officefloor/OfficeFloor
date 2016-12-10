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
package net.officefloor.eclipse.skin.standard;

import net.officefloor.eclipse.skin.OfficeFloorSkin;
import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.section.SectionFigureFactory;
import net.officefloor.eclipse.skin.standard.desk.StandardDeskFigureFactory;
import net.officefloor.eclipse.skin.standard.office.StandardOfficeFigureFactory;
import net.officefloor.eclipse.skin.standard.officefloor.StandardOfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.standard.section.StandardSectionFigureFactory;

/**
 * The standard {@link OfficeFloorSkin}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorSkin implements OfficeFloorSkin {

	/**
	 * {@link StandardOfficeFloorColours}.
	 */
	private static final StandardOfficeFloorColours colours = new StandardOfficeFloorColours();

	/**
	 * Obtains the {@link StandardOfficeFloorColours}.
	 *
	 * TODO provide means to configure these colours in properties.
	 *
	 * @return {@link StandardOfficeFloorColours}.
	 */
	public static StandardOfficeFloorColours getColours() {
		return colours;
	}

	/**
	 * {@link DeskFigureFactory}.
	 */
	private final DeskFigureFactory deskFigureFactory = new StandardDeskFigureFactory();

	/**
	 * {@link OfficeFigureFactory}.
	 */
	private final OfficeFigureFactory officeFigureFactory = new StandardOfficeFigureFactory();

	/**
	 * {@link OfficeFloorFigureFactory}.
	 */
	private final OfficeFloorFigureFactory officeFloorFigureFactory = new StandardOfficeFloorFigureFactory();

	/**
	 * {@link SectionFigureFactory}.
	 */
	private final SectionFigureFactory roomFigureFactory = new StandardSectionFigureFactory();

	/*
	 * ==================== OfficeFloorSkin ================================
	 */

	@Override
	public DeskFigureFactory getDeskFigureFactory() {
		return this.deskFigureFactory;
	}

	@Override
	public OfficeFigureFactory getOfficeFigureFactory() {
		return this.officeFigureFactory;
	}

	@Override
	public OfficeFloorFigureFactory getOfficeFloorFigureFactory() {
		return this.officeFloorFigureFactory;
	}

	@Override
	public SectionFigureFactory getSectionFigureFactory() {
		return this.roomFigureFactory;
	}

}