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
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * {@link OfficeFloorFigure} for the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TemplateFigure extends OfficeFloorFigure {

	/**
	 * Specifies the template application path.
	 * 
	 * @param templateDisplayName
	 *            Application path for the template.
	 */
	void setTemplateApplicationPath(String templateDisplayName);

	/**
	 * Obtains the template application path {@link IFigure}.
	 * 
	 * @return Template application path {@link IFigure}.
	 */
	IFigure getTemplateApplicationPathFigure();

	/**
	 * Flags whether template is secure.
	 * 
	 * @param isSecure
	 *            <code>true</code> if template is secure.
	 */
	void setTemplateSecure(boolean isSecure);

}