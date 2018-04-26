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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.woof.SectionInputFigure;
import net.officefloor.eclipse.skin.woof.SectionInputFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.woof.WoofAccessOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link SectionInputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionInputFigure extends AbstractOfficeFloorFigure
		implements SectionInputFigure {

	/**
	 * {@link SectionInputFigureContext}.
	 */
	private final SectionInputFigureContext context;

	/**
	 * Name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SectionInputFigureContext}.
	 */
	public StandardSectionInputFigure(SectionInputFigureContext context) {
		this.context = context;

		// Create figure
		LabelConnectorFigure connector = new LabelConnectorFigure(
				this.getDisplayName(), ConnectorDirection.WEST,
				CommonWoofColours.CONNECTIONS());
		this.name = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				WoofTemplateOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofSectionOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofAccessOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofExceptionToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(WoofStartToWoofSectionInputModel.class,
				anchor);

		this.setFigure(connector);
	}

	/**
	 * Obtains the display name.
	 * 
	 * @return Display name.
	 */
	private String getDisplayName() {
		String uri = this.context.getUri();
		return this.context.getSectionInputName()
				+ (EclipseUtil.isBlank(uri) ? "" : " : " + uri);
	}

	/*
	 * ============================ SectionInputFigure =======================
	 */

	@Override
	public void setSectionInputName(String sectionInputName) {
		this.name.setText(this.getDisplayName());
	}

	@Override
	public void setUri(String uri) {
		this.name.setText(this.getDisplayName());
	}

	@Override
	public IFigure getUriFigure() {
		return this.name;
	}

}