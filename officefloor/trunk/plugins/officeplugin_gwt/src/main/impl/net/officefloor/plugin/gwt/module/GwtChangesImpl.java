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
package net.officefloor.plugin.gwt.module;

import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link GwtChanges} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtChangesImpl implements GwtChanges {

	/**
	 * {@link GwtModuleRepository}.
	 */
	private final GwtModuleRepository repository;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * {@link GwtFailureListener}.
	 */
	private final GwtFailureListener listener;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link GwtModuleRepository}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param listener
	 *            {@link GwtFailureListener}.
	 */
	public GwtChangesImpl(GwtModuleRepository repository,
			ConfigurationContext context, GwtFailureListener listener) {
		this.repository = repository;
		this.context = context;
		this.listener = listener;
	}

	/*
	 * ===================== GwtChanges ========================
	 */

	@Override
	public String createGwtModulePath(GwtModuleModel module) {
		return this.repository.createGwtModulePath(module);
	}

	@Override
	public GwtModuleModel retrieveGwtModule(String gwtModulePath) {
		try {
			return this.repository.retrieveGwtModule(gwtModulePath,
					this.context);
		} catch (Exception ex) {
			this.listener.notifyFailure("Failed to retrieve GWT Module", ex);
			return null; // not able to obtain GWT Module
		}
	}

	@Override
	public Change<GwtModuleModel> updateGwtModule(final GwtModuleModel module,
			final String existingGwtModulePath) {

		// Return the change to the GWT Module
		return new AbstractChange<GwtModuleModel>(module, "Update GWT Module") {

			/**
			 * Existing {@link GwtModuleModel}.
			 */
			private GwtModuleModel existingModule = null;

			/**
			 * New GWT Module path.
			 */
			private String newGwtModulePath = null;

			/*
			 * ===================== Change ===================================
			 */

			@Override
			public void apply() {
				try {
					// Obtain the existing GWT Module
					this.existingModule = GwtChangesImpl.this.repository
							.retrieveGwtModule(existingGwtModulePath,
									GwtChangesImpl.this.context);

					// Apply changes the configuration
					this.newGwtModulePath = GwtChangesImpl.this.repository
							.storeGwtModule(module,
									GwtChangesImpl.this.context,
									existingGwtModulePath);
				} catch (Exception ex) {
					GwtChangesImpl.this.listener.notifyFailure(
							"Failure applying GWT Module changes", ex);
				}
			}

			@Override
			public void revert() {
				try {
					if (this.existingModule == null) {
						// Remove the configuration (as was just created)
						GwtChangesImpl.this.repository.deleteGwtModule(
								this.newGwtModulePath,
								GwtChangesImpl.this.context);
					} else {
						// Revert the configuration (as updated)
						GwtChangesImpl.this.repository.storeGwtModule(
								this.existingModule,
								GwtChangesImpl.this.context,
								this.newGwtModulePath);
					}
				} catch (Exception ex) {
					GwtChangesImpl.this.listener.notifyFailure(
							"Failure reverting GWT Module changes", ex);
				}
			}
		};
	}

}