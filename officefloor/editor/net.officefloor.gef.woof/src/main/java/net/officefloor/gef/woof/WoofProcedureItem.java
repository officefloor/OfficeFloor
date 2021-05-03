/*-
 * #%L
 * [bundle] Section Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.woof;

import java.util.Map;

import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.item.AbstractProcedureItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofStartToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofProcedureModel;

/**
 * Configuration for the {@link WoofProcedureModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofProcedureItem extends
		AbstractProcedureItem<WoofModel, WoofEvent, WoofChanges, WoofProcedureModel, WoofProcedureEvent, WoofProcedureItem> {

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public WoofProcedureModel prototype() {
		return new WoofProcedureModel("Procedure", null, null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getWoofProcedures(), WoofEvent.ADD_WOOF_PROCEDURE,
				WoofEvent.REMOVE_WOOF_PROCEDURE);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofProcedureModel itemModel) {
		parentModel.addWoofProcedure(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofProcedureName(), WoofProcedureEvent.CHANGE_WOOF_PROCEDURE_NAME);
	}

	@Override
	protected WoofProcedureItem createItem() {
		return new WoofProcedureItem();
	}

	@Override
	protected String getSectionName(WoofProcedureModel model) {
		return model.getWoofProcedureName();
	}

	@Override
	protected String getResource(WoofProcedureModel model) {
		return model.getResource();
	}

	@Override
	protected String getSourceName(WoofProcedureModel model) {
		return model.getSourceName();
	}

	@Override
	protected String getProcedureName(WoofProcedureModel model) {
		return model.getProcedureName();
	}

	@Override
	protected PropertyList getProcedureProperties(WoofProcedureModel model) {
		return this.translateToPropertyList(model.getProperties(), p -> p.getName(), p -> p.getValue());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends ConnectionModel>[] getInputConnectionClasses() {
		return new Class[] { WoofHttpContinuationToWoofProcedureModel.class, WoofHttpInputToWoofProcedureModel.class,
				WoofExceptionToWoofProcedureModel.class, WoofStartToWoofProcedureModel.class,
				WoofSectionOutputToWoofProcedureModel.class, WoofSecurityOutputToWoofProcedureModel.class,
				WoofTemplateOutputToWoofProcedureModel.class, WoofProcedureNextToWoofProcedureModel.class,
				WoofProcedureOutputToWoofProcedureModel.class };
	}

	@Override
	protected AbstractItem<WoofModel, WoofChanges, WoofProcedureModel, WoofProcedureEvent, ?, ?> createNextItem() {
		return new WoofProcedureNextItem();
	}

	@Override
	protected AbstractItem<WoofModel, WoofChanges, WoofProcedureModel, WoofProcedureEvent, ?, ?> createOutputItem() {
		return new WoofProcedureOutputItem();
	}

	@Override
	protected Change<WoofProcedureModel> addProcedure(WoofChanges operations, String name, String resource,
			String sourceName, String procedure, PropertyList properties, ProcedureType procedureType) {
		return operations.addProcedure(name, resource, sourceName, procedure, properties, procedureType);
	}

	@Override
	protected Change<WoofProcedureModel> refactorProcedure(WoofChanges operations, WoofProcedureModel model,
			String name, String resource, String sourceName, String procedure, PropertyList properties,
			ProcedureType procedureType, Map<String, String> outputNameMapping) {
		return operations.refactorProcedure(model, name, resource, sourceName, procedure, properties, procedureType,
				outputNameMapping);
	}

	@Override
	protected Change<WoofProcedureModel> removeProcedure(WoofChanges operations, WoofProcedureModel model) {
		return operations.removeProcedure(model);
	}

}
