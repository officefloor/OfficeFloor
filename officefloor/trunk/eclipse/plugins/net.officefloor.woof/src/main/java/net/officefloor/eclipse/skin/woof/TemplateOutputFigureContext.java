/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

import org.eclipse.draw2d.IFigure;

/**
 * Context for the {@link WoofTemplateOutputModel} {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TemplateOutputFigureContext {

	/**
	 * Obtains the name of the {@link HttpTemplate} output.
	 * 
	 * @return Name of the {@link HttpTemplate} output.
	 */
	String getTemplateOutputName();

	/**
	 * Flags if the render complete output.
	 * 
	 * @return <code>true</code> if render complete output.
	 */
	boolean isRenderCompleteOutput();

}