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
package net.officefloor.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionService;

/**
 * Validate changing the template URI with
 * {@link GwtWoofTemplateExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateUriWithGwtTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure can change template URI.
	 */
	public void testUriChange() throws Exception {

		final String EXISTING_MODULE_PATH = "net/example/uri.gwt.xml";
		final String CHANGE_URI = "change";
		final String ENTRY_POINT_CLASS = "net.example.client.ExampleEntryPoint";
		final GwtModuleModel module = new GwtModuleModel("uri",
				ENTRY_POINT_CLASS, null);

		// Record updating template URI for GWT
		GwtChanges changes = this.getGwtChanges();
		this.recordReturn(changes,
				changes.retrieveGwtModule(EXISTING_MODULE_PATH), module);
		this.recordGwtModulePath("net/example/change.gwt.xml");
		Change<GwtModuleModel> gwtChange = this.recordGwtUpdate(CHANGE_URI,
				ENTRY_POINT_CLASS, EXISTING_MODULE_PATH);
		this.recordReturn(gwtChange, gwtChange.getConflicts(), new Conflict[0]);
		gwtChange.apply();

		// Record reverting and applying in asserting the change
		gwtChange.revert();
		gwtChange.apply();
		gwtChange.revert();

		// Test
		this.replayMockObjects();

		// Change the template URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "change");

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

}