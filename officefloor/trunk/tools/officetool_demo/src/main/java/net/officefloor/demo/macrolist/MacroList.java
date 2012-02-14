/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.demo.macrolist;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.record.RecordListener;

public class MacroList implements RecordListener {

	/**
	 * List of the {@link MacroItem} instances.
	 */
	private final List<MacroItem> items = new ArrayList<MacroItem>();

	/**
	 * {@link MacroListListener}.
	 */
	private final MacroListListener listener;

	/**
	 * {@link MacroIndexFactory}.
	 */
	private final MacroIndexFactory indexFactory;

	/**
	 * Initiate.
	 * 
	 * @param indexFactory
	 *            {@link MacroIndexFactory}.
	 * @param listener
	 *            {@link MacroListListener}.
	 */
	public MacroList(MacroIndexFactory indexFactory, MacroListListener listener) {
		this.indexFactory = indexFactory;
		this.listener = listener;
	}

	/**
	 * Obtains the number of {@link MacroItem} instances within this list.
	 * 
	 * @return Number of {@link MacroItem} instances within this list.
	 */
	public int size() {
		return this.items.size();
	}

	/**
	 * Obtains the {@link MacroItem} at the index within this list.
	 * 
	 * @param index
	 *            Index of the {@link MacroItem} to obtain.
	 * @return {@link MacroItem} at the index.
	 * @throws IndexOutOfBoundsException
	 *             If index does not correspond with a {@link MacroItem}.
	 */
	public MacroItem getItem(int index) throws IndexOutOfBoundsException {
		return this.items.get(index);
	}

	/**
	 * Removes the {@link MacroItem} at the index from the list.
	 * 
	 * @param index
	 *            Index of the {@link MacroItem} to remove.
	 * @throws IndexOutOfBoundsException
	 *             If index does not correspond with a {@link MacroItem}.
	 */
	public void removeItem(int index) throws IndexOutOfBoundsException {

		// Remove the macro item
		MacroItem item = this.items.remove(index);

		// Notify item removed
		this.listener.macroRemoved(item, index);
	}

	/*
	 * ====================== RecordListener ========================
	 */

	@Override
	public void addMacro(Macro macro) {

		// Create the item for the macro
		MacroItem item = new MacroItemImpl(macro);

		// Use default of append to list
		int index = this.items.size();
		if (this.indexFactory != null) {
			int specifiedIndex = this.indexFactory.createMacroIndex();
			if (specifiedIndex >= 0) {
				// Specify the index
				index = specifiedIndex;
			}
		}

		// Add macro to the list (keeping track of its index)
		this.items.add(index, item);

		// Notify macro added
		this.listener.macroAdded(item, index);
	}

	/**
	 * {@link MacroItem} implementation.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class MacroItemImpl implements MacroItem {

		/**
		 * {@link Macro}.
		 */
		private final Macro macro;

		/**
		 * Initiate.
		 * 
		 * @param macro
		 *            {@link Macro}.
		 */
		public MacroItemImpl(Macro macro) {
			this.macro = macro;
		}

		/*
		 * =================== MacroItem ==============================
		 */

		@Override
		public Macro getMacro() {
			return this.macro;
		}
	}

}