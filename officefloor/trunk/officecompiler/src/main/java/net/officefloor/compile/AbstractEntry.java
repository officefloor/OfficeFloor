/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile;

import net.officefloor.model.Model;

/**
 * Abstract entry.
 * 
 * @author Daniel
 */
public abstract class AbstractEntry<B, M> {

	/**
	 * Id of this entry.
	 */
	private final String id;

	/**
	 * Builder for this entry.
	 */
	private final B builder;

	/**
	 * Model for this entry.
	 */
	private final M model;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of this entry.
	 * @param builder
	 *            Builder for this entry.
	 * @param model
	 *            Model for this entry.
	 */
	public AbstractEntry(String id, B builder, M model) {
		this.id = id;
		this.builder = builder;
		this.model = model;
	}

	/**
	 * Obtains the Id of this entry.
	 * 
	 * @return Id of this entry.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Obtains the builder for this entry.
	 * 
	 * @return Builder for this entry.
	 */
	public B getBuilder() {
		return this.builder;
	}

	/**
	 * Obtains the model for this entry.
	 * 
	 * @return Model for this entry.
	 */
	public M getModel() {
		return this.model;
	}
	
	/**
	 * Helper method to ensure that {@link Model} is obtained from the input
	 * {@link ModelEntryMap}.
	 * 
	 * @param entry
	 *            {@link AbstractEntry}.
	 * @param map
	 *            {@link ModelEntryMap}.
	 * @param notFoundMessage
	 *            Message to use if not found.
	 * @return {@link Model}.
	 * @throws Exception
	 *             If {@link Model} not found.
	 */
	protected <MD extends Model, E extends AbstractEntry<?, ?>> MD getModel(
			E entry, ModelEntryMap<MD, E> map, String notFoundMessage)
			throws Exception {
		// Obtain the model
		MD model = map.getModel(entry);

		// Ensure have a model
		if (model == null) {
			throw new Exception(notFoundMessage);
		}

		// Return the model
		return model;
	}

	/**
	 * Helper method to ensure that {@link AbstractEntry} is obtained from the
	 * input {@link ModelEntryMap}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param map
	 *            {@link ModelEntryMap}.
	 * @param notFoundMessage
	 *            Message to use if not found.
	 * @return {@link AbstractEntry}.
	 * @throws Exception
	 *             If {@link AbstractEntry} not found.
	 */
	protected <MD extends Model, E extends AbstractEntry<?, ?>> E getEntry(
			MD model, ModelEntryMap<MD, E> map, String notFoundMessage)
			throws Exception {
		// Obtain the entry
		E entry = map.getEntry(model);

		// Ensure have the entry
		if (entry == null) {
			throw new Exception(notFoundMessage);
		}

		// Return the entry
		return entry;
	}
}
