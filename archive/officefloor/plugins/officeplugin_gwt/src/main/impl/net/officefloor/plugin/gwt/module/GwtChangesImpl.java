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
			return this.repository.retrieveGwtModuleModel(gwtModulePath,
					this.context);
		} catch (Exception ex) {
			this.listener.notifyFailure("Failed to retrieve GWT Module", ex);
			return null; // not able to obtain GWT Module
		}
	}

	@Override
	public Change<GwtModuleModel> updateGwtModule(final GwtModuleModel module,
			final String existingGwtModulePath) {

		// Obtain the new GWT Module path
		final String newGwtModulePath = this.repository
				.createGwtModulePath(module);

		// Return the change to the GWT Module
		return new AbstractChange<GwtModuleModel>(module, "Update GWT Module") {

			/**
			 * Override {@link GwtModule}.
			 */
			private GwtModule overrideModule = null;

			/**
			 * Existing {@link GwtModule}.
			 */
			private GwtModule existingModule = null;

			/*
			 * ===================== Change ===================================
			 */

			@Override
			public void apply() {
				try {

					// Obtain potential GWT Module being overridden
					this.overrideModule = GwtChangesImpl.this.repository
							.retrieveGwtModule(newGwtModulePath,
									GwtChangesImpl.this.context);

					// Obtain the existing module (if relocating)
					this.existingModule = null; // reset
					if ((existingGwtModulePath != null)
							&& (!(existingGwtModulePath
									.equals(newGwtModulePath)))) {
						this.existingModule = GwtChangesImpl.this.repository
								.retrieveGwtModule(existingGwtModulePath,
										GwtChangesImpl.this.context);
					}

					// Apply changes to the configuration
					GwtChangesImpl.this.repository.storeGwtModule(module,
							GwtChangesImpl.this.context, existingGwtModulePath);

				} catch (Exception ex) {
					GwtChangesImpl.this.listener.notifyFailure(
							"Failure applying GWT Module changes", ex);
				}
			}

			@Override
			public void revert() {
				try {

					// Reinstate the possible existing module
					if (this.existingModule != null) {
						GwtChangesImpl.this.repository.storeGwtModule(
								this.existingModule,
								GwtChangesImpl.this.context);
					}

					// Reinstate the previous module (from override)
					if (this.overrideModule != null) {
						GwtChangesImpl.this.repository.storeGwtModule(
								this.overrideModule,
								GwtChangesImpl.this.context);
					} else {
						// Nothing overridden, so delete
						GwtChangesImpl.this.repository.deleteGwtModule(
								newGwtModulePath, GwtChangesImpl.this.context);
					}

				} catch (Exception ex) {
					GwtChangesImpl.this.listener.notifyFailure(
							"Failure reverting GWT Module changes", ex);
				}
			}
		};
	}
}