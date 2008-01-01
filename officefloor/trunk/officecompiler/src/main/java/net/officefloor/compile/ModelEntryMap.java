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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.Model;

/**
 * Maps {@link Model} to {@link AbstractEntry} and vice versa.
 * 
 * @author Daniel
 */
class ModelEntryMap<M extends Model, E extends AbstractEntry<?, ?>> {

	/**
	 * Maps {@link Model} to {@link AbstractEntry}.
	 */
	private final Map<M, E> modelToEntry = new HashMap<M, E>();

	/**
	 * Maps {@link AbstractEntry} to {@link Model}.
	 */
	private final Map<E, M> entryToModel = new HashMap<E, M>();

	/**
	 * Registers a {@link Model} to {@link AbstractEntry} mapping.
	 * 
	 * @param key
	 *            Key.
	 * @param value
	 *            Value.
	 */
	public void put(M model, E entry) {
		// Map model to entry
		this.modelToEntry.put(model, entry);

		// Map entry to model
		this.entryToModel.put(entry, model);
	}

	/**
	 * Obtains the {@link AbstractEntry}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return {@link AbstractEntry} or <code>null</code> if not mapped.
	 */
	public E getEntry(M model) {
		return this.modelToEntry.get(model);
	}

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @param entry
	 *            {@link AbstractEntry}.
	 * @return {@link Model}. or <code>null</code> if not mapped.
	 */
	public M getModel(E entry) {
		return this.entryToModel.get(entry);
	}
}
