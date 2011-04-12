/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.model.woof.WoofModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofFigureFactory {

	/**
	 * Creates the {@link TemplateFigure}.
	 * 
	 * @param context
	 *            {@link TemplateFigureContext}.
	 * @return {@link TemplateFigure}.
	 */
	TemplateFigure createTemplateFigure(TemplateFigureContext context);

	/**
	 * Creates the {@link TemplateOutputFigure}.
	 * 
	 * @param context
	 *            {@link TemplateOutputFigureContext}.
	 * @return {@link TemplateOutputFigure}.
	 */
	TemplateOutputFigure createTemplateOutputFigure(
			TemplateOutputFigureContext context);

	/**
	 * Creates the {@link SectionFigure}.
	 * 
	 * @param context
	 *            {@link SectionFigureContext}.
	 * @return {@link SectionFigure}.
	 */
	SectionFigure createSectionFigure(SectionFigureContext context);

	/**
	 * Creates the {@link SectionInputFigure}.
	 * 
	 * @param context
	 *            {@link SectionInputFigureContext}.
	 * @return {@link SectionInputFigure}.
	 */
	SectionInputFigure createSectionInputFigure(
			SectionInputFigureContext context);

	/**
	 * Creates the {@link SectionOutputFigure}.
	 * 
	 * @param context
	 *            {@link SectionOutputFigureContext}.
	 * @return {@link SectionOutputFigure}.
	 */
	SectionOutputFigure createSectionOutputFigure(
			SectionOutputFigureContext context);

}