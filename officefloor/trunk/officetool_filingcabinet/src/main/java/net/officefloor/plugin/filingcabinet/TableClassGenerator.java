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
package net.officefloor.plugin.filingcabinet;

import java.util.HashSet;
import java.util.Set;

/**
 * Generates the class for the table.
 * 
 * @author Daniel
 */
public class TableClassGenerator {

	/**
	 * Generates the contents of the class for the input table.
	 * 
	 * @param table
	 *            {@link TableMetaData}.
	 * @return Contents of the class.
	 */
	public String generate(TableMetaData table) {

		// Create the buffer to contain the contents
		StringBuilder contents = new StringBuilder();

		// PACAKGE
		this.writePackageDeclaration(contents, table.getPackageName());

		// IMPORTS
		Set<String> classes = new HashSet<String>();
		for (ColumnMetaData column : table.getColumns()) {
			classes.add(column.getJavaType().getName());
		}
		contents.append("\n");
		this.writeImports(contents, classes);

		// CLASS DECLARATION
		contents.append("\n");
		this.writeClassDeclaration(contents, table.getSimpleClassName(), null);

		// FIELDS
		for (ColumnMetaData column : table.getColumns()) {
			contents.append("\n");
			this.writeField(contents, column.getJavaType().getName(), column
					.getColumnName(), "Column " + column.getColumnName() + ".");
		}

		// Close the class
		contents.append("}\n");

		// Return the contents
		return contents.toString();
	}

	/**
	 * Writes the inputs.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param importedClasses
	 *            Imported {@link Class} instances.
	 */
	protected void writeImports(StringBuilder contents,
			Set<String> importedClasses) {
		for (String importedClass : importedClasses) {

			// Need not import java.lang classes
			if (importedClass.startsWith("java.lang.")) {
				continue;
			}

			// Import the class
			contents.append("import " + importedClass + ";\n");
		}
	}

	/**
	 * Writes the package declaration.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param packageName
	 *            Package name.
	 */
	protected void writePackageDeclaration(StringBuilder contents,
			String packageName) {
		contents.append("package " + packageName + ";\n");
	}

	/**
	 * Writes the class declaration.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param className
	 *            Name of the class.
	 * @param extendsName
	 *            Name of class being extended. May be <code>null</code> if
	 *            not extending a class.
	 * @param interfaceNames
	 *            Names of the interfaces being implemented.
	 */
	protected void writeClassDeclaration(StringBuilder contents,
			String className, String extendsName, String... interfaceNames) {
		contents.append("public class " + className + " ");
		if (extendsName != null) {
			contents.append("extends " + extendsName + " ");
		}
		if (interfaceNames.length > 0) {
			contents.append("implements ");
			boolean isFirst = true;
			for (String interfaceName : interfaceNames) {
				if (isFirst) {
					isFirst = false;
				} else {
					contents.append(", ");
				}
				contents.append(interfaceName);
			}
		}
		contents.append("{\n");
	}

	/**
	 * Writes the field.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param propertyType
	 *            Property type.
	 * @param propertyName
	 *            Property name.
	 * @param documentation
	 *            Documentation.
	 */
	protected void writeField(StringBuilder contents, String propertyType,
			String propertyName, String documentation) {
		this.writeDocumentation(contents, 1, documentation);
		this.writeIndent(contents, 1);
		contents.append("private " + propertyType + " " + propertyName + ";\n");
	}

	/**
	 * Writes the getter.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param propertyType
	 *            Property type.
	 * @param propertyName
	 *            Property name.
	 * @param documentation
	 *            Documentation.
	 */
	protected void writeGetter(StringBuilder contents, String propertyType,
			String propertyName, String documentation) {
		// TODO provide getter
	}

	/**
	 * Writes the documentation.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param indent
	 *            Number of indents.
	 * @param documentation
	 *            Documentation. May be <code>null</code>.
	 */
	protected void writeDocumentation(StringBuilder contents, int indent,
			String documentation) {
		// Provide documentation if provided
		if (documentation != null) {
			String[] docLines = documentation.split("\n");
			this.writeIndent(contents, indent);
			contents.append("/**\n");
			for (String docLine : docLines) {
				this.writeIndent(contents, indent);
				contents.append(" * " + docLine + "\n");
			}
			this.writeIndent(contents, indent);
			contents.append(" */\n");
		}
	}

	/**
	 * Writes the indent.
	 * 
	 * @param contents
	 *            Buffer of contents.
	 * @param indent
	 *            Number of indents.
	 */
	protected void writeIndent(StringBuilder contents, int indent) {
		for (int i = 0; i < indent; i++) {
			contents.append("    ");
		}
	}
}
