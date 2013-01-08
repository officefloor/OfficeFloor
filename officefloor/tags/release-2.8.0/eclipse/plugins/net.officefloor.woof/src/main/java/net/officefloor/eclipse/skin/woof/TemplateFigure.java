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
import net.officefloor.model.woof.WoofTemplateModel;

/**
 * {@link OfficeFloorFigure} for the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TemplateFigure extends OfficeFloorFigure {

	/**
	 * Specifies the template display name.
	 * 
	 * @param templateDisplayName
	 *            Display name for the template.
	 */
	void setTemplateDisplayName(String templateDisplayName);

	/**
	 * Obtains the template display name {@link IFigure}.
	 * 
	 * @return Template display name {@link IFigure}.
	 */
	IFigure getTemplateDisplayFigure();

}