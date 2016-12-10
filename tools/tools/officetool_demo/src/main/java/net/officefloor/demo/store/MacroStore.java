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

package net.officefloor.demo.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.demo.macro.Macro;

/**
 * Store providing ability to store/retrieve {@link Macro} listings.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroStore {

	/**
	 * Line separator.
	 */
	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	/**
	 * End of line token for storing.
	 */
	private static final String EOL_TOKEN = "{n}";

	/**
	 * Stores the {@link Macro} into the {@link Writer}.
	 * 
	 * @param macros
	 *            Listing of {@link Macro} instances to be stored.
	 * @param writer
	 *            {@link Writer} to receive the serialised content of the
	 *            {@link Macro} objects.
	 * @throws IOException
	 *             If fails to write {@link Macro} objects.
	 */
	public void store(Macro[] macros, Writer writer) throws IOException {

		// Write the content for macros
		for (Macro macro : macros) {

			// Determine class name
			String className;
			if (macro.getClass().getPackage().equals(Macro.class.getPackage())) {
				// Use the simple name as contained in macro package
				className = macro.getClass().getSimpleName();
			} else {
				// Use the fully qualified name
				className = macro.getClass().getName();
			}

			// Obtain the configuration memento for the macro
			String memento = macro.getConfigurationMemento();

			// Ensure memento is single line
			memento = memento.replace(LINE_SEPARATOR, EOL_TOKEN);

			// Write the macro details
			writer.write(className + ":" + memento + "\n");
		}
	}

	/**
	 * Retrieves the {@link Macro} listing from the {@link Reader}.
	 * 
	 * @param reader
	 *            {@link Reader} containing the serialised content of the
	 *            {@link Macro} objects.
	 * @return Listing of {@link Macro} instances.
	 * @throws IOException
	 *             If fails to read content.
	 * @throws MacroRetrieveException
	 *             If fails to retrieve the stored {@link Macro}.
	 */
	public Macro[] retrieve(Reader reader) throws IOException,
			MacroRetrieveException {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Read the macros from the reader
		BufferedReader content = new BufferedReader(reader);
		String line;
		List<Macro> macros = new LinkedList<Macro>();
		while ((line = content.readLine()) != null) {

			// Ignore blank lines
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}

			// Obtain the details of the macro
			int separatorIndex = line.indexOf(':');
			if (separatorIndex < 0) {
				// Line is invalid as should have separator
				throw new IOException("Invalid line [" + line + "]");
			}
			String className = line.substring(0, separatorIndex);
			// +1 to ignore separator
			String configuration = line.substring(separatorIndex + 1);

			// Obtain the macro class
			Class<?> clazz = this.loadClass(className, classLoader);
			if (clazz == null) {
				// Try within macro package
				clazz = this.loadClass(Macro.class.getPackage().getName() + "."
						+ className, classLoader);
			}
			if (clazz == null) {
				// Must have macro class
				throw new MacroRetrieveException("Could not find macro class '"
						+ className + "'");
			}

			// Instantiate the macro
			Object object;
			try {
				object = clazz.newInstance();
			} catch (Exception ex) {
				throw new MacroRetrieveException("Failed to instantiate macro "
						+ clazz.getName(), ex);
			}
			if (!(object instanceof Macro)) {
				throw new MacroRetrieveException("Macro "
						+ object.getClass().getName() + " must implement "
						+ Macro.class.getName());
			}
			Macro macro = (Macro) object;

			// Transform to multi-line content
			configuration = configuration.replace(EOL_TOKEN, LINE_SEPARATOR);

			// Configure the macro
			macro.setConfigurationMemento(configuration);

			// Register the macro for return
			macros.add(macro);
		}

		// Return the macros
		return macros.toArray(new Macro[0]);
	}

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param className
	 *            Class name.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link Class} or <code>null</code> if could not find.
	 */
	private Class<?> loadClass(String className, ClassLoader classLoader) {
		try {
			// Return the loaded class
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			// Can not find class
			return null;
		}
	}

}