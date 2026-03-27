/*-
 * #%L
 * [bundle] Section Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
