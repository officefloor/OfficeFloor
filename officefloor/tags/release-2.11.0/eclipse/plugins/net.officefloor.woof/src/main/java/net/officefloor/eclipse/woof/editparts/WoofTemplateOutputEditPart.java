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
package net.officefloor.eclipse.woof.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateOutputModel.WoofTemplateOutputEvent;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofTemplateOutputEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateOutputEditPart
		extends
		AbstractOfficeFloorEditPart<WoofTemplateOutputModel, WoofTemplateOutputEvent, TemplateOutputFigure>
		implements TemplateOutputFigureContext {

	@Override
	protected TemplateOutputFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createTemplateOutputFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getWoofTemplate());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getWoofSectionInput());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getWoofAccessInput());
		EclipseUtil.addToList(models, this.getCastedModel().getWoofResource());
	}

	@Override
	protected Class<WoofTemplateOutputEvent> getPropertyChangeEventType() {
		return WoofTemplateOutputEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofTemplateOutputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_TEMPLATE_OUTPUT_NAME:
			this.getOfficeFloorFigure().setTemplateOutputName(
					this.getTemplateOutputName());
			break;

		case CHANGE_WOOF_TEMPLATE:
		case CHANGE_WOOF_SECTION_INPUT:
		case CHANGE_WOOF_ACCESS_INPUT:
		case CHANGE_WOOF_RESOURCE:
			this.refreshSourceConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
			// No visual change
			break;
		}
	}

	/*
	 * ==================== TemplateOutputFigureContext ==============
	 */

	@Override
	public String getTemplateOutputName() {
		return this.getCastedModel().getWoofTemplateOutputName();
	}

	@Override
	public boolean isRenderCompleteOutput() {
		return HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME.equals(this
				.getCastedModel().getWoofTemplateOutputName());
	}

}