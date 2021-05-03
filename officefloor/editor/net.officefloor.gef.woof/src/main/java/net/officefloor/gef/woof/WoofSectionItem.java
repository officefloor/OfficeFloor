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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.item.AbstractSectionItem;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;

/**
 * Configuration for the {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionItem extends
		AbstractSectionItem<WoofModel, WoofEvent, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionItem> {

	/*
	 * =================== AbstractSectionItem ====================
	 */

	@Override
	public WoofSectionModel prototype() {
		return new WoofSectionModel("Section", null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getWoofSections(), WoofEvent.ADD_WOOF_SECTION,
				WoofEvent.REMOVE_WOOF_SECTION);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofSectionModel itemModel) {
		parentModel.addWoofSection(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionName(), WoofSectionEvent.CHANGE_WOOF_SECTION_NAME);
	}

	@Override
	protected WoofSectionItem createItem() {
		return new WoofSectionItem();
	}

	@Override
	protected String getSectionName(WoofSectionModel model) {
		return model.getWoofSectionName();
	}

	@Override
	protected String getSectionSourceClassName(WoofSectionModel model) {
		return model.getSectionSourceClassName();
	}

	@Override
	protected String getSectionLocation(WoofSectionModel model) {
		return model.getSectionLocation();
	}

	@Override
	protected PropertyList getSectionProperties(WoofSectionModel model) {
		return this.translateToPropertyList(model.getProperties(), p -> p.getName(), p -> p.getValue());
	}

	@Override
	protected AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, ?, ?> createInputItem() {
		return new WoofSectionInputItem();
	}

	@Override
	protected AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, ?, ?> createOutputItem() {
		return new WoofSectionOutputItem();
	}

	@Override
	protected Change<WoofSectionModel> addSection(WoofChanges operations, String name, String sourceClassName,
			String location, PropertyList properties, SectionType sectionType) {
		return operations.addSection(name, sourceClassName, location, properties, sectionType);
	}

	@Override
	protected Change<WoofSectionModel> refactorSection(WoofChanges operations, WoofSectionModel model, String name,
			String sourceClassName, String location, PropertyList properties, SectionType sectionType,
			Map<String, String> inputNameMapping, Map<String, String> outputNameMapping) {
		return operations.refactorSection(model, name, sourceClassName, location, properties, sectionType,
				inputNameMapping, outputNameMapping);
	}

	@Override
	protected Change<WoofSectionModel> removeSection(WoofChanges operations, WoofSectionModel model) {
		return operations.removeSection(model);
	}

}
