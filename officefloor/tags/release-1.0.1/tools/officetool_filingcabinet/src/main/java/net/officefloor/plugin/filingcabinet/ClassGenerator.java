/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.filingcabinet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the class java code.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGenerator {

	/**
	 * Class name.
	 */
	private final String className;

	/**
	 * Package name.
	 */
	private final String packageName;

	/**
	 * Classes being used by the java code.
	 */
	private final Set<String> importedClasses = new HashSet<String>();

	/**
	 * Class being extended.
	 */
	private String extendsClassName = null;

	/**
	 * Interfaces being implemented.
	 */
	private final Set<String> implementingInterfaceNames = new HashSet<String>();

	/**
	 * Constructors.
	 */
	private final Set<String> constructors = new HashSet<String>();

	/**
	 * Mapping of field name to field declaration.
	 */
	private final Map<String, String> fields = new HashMap<String, String>();

	/**
	 * Mapping of property name to property accessor and mutator.
	 */
	private final Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Mapping of table name to linkTo table method.
	 */
	private final Map<String, String> linkToTables = new HashMap<String, String>();

	/**
	 * Load method.
	 */
	private String loadMethod = "";

	/**
	 * Retrieve methods by the method name.
	 */
	public final Map<String, String> retrieveMethods = new HashMap<String, String>();

	/**
	 * Initiate.
	 * 
	 * @param className
	 *            Class name.
	 * @param packageParts
	 *            Package name parts.
	 */
	public ClassGenerator(String className, String... packageParts) {
		this.className = className;

		// Create the package name
		StringBuilder packageName = new StringBuilder();
		boolean isFirst = true;
		for (String packagePart : packageParts) {
			if (packagePart == null) {
				// Ignore null entries
				continue;
			}
			packageName.append(isFirst ? "" : ".");
			isFirst = false;
			packageName.append(packagePart);
		}
		this.packageName = packageName.toString();
	}

	/**
	 * Adds as an imported class and returns its simple class name.
	 * 
	 * @param className
	 *            Class name.
	 * @return Simple class name.
	 */
	public String addImport(String className) {

		// Add as imported class
		this.importedClasses.add(className);

		// Split by '.' and last part is simple class name
		String[] classNameParts = className.split("\\.");
		return classNameParts[classNameParts.length - 1];
	}

	/**
	 * Convenience method to add an imported {@link Class}.
	 * 
	 * @param clazz
	 *            Imported {@link Class}.
	 * @return Simple name of the {@link Class}.
	 */
	public String addImport(Class<?> clazz) {
		return this.addImport(clazz.getName());
	}

	/**
	 * Adds a constructor.
	 * 
	 * @param columns
	 *            Listing of {@link ColumnMetaData} instances specifying the
	 *            fields to initialise.
	 */
	public void addConstructor(ColumnMetaData... columns) {

		StringBuilder segment = new StringBuilder();

		// Determine if default constructor
		if (columns.length == 0) {
			// Default constructor
			this.writeDocumentation(segment, 1, "Default constructor.");
			this.writeIndent(segment, 1).append(
					"public " + this.className + "() {\n");
			this.writeIndent(segment, 1).append("}\n\n");
		} else {
			// Initialise from fields
			this.writeDocumentation(segment, 1, "Initialise.");
			this.writeIndent(segment, 1).append(
					"public " + this.className + "(");
			boolean isFirst = true;
			for (ColumnMetaData column : columns) {
				segment.append(isFirst ? "" : ", ");
				isFirst = false;
				String columnType = this.addImport(column.getJavaType());
				segment.append(columnType + " " + column.getFieldName());
			}
			segment.append(") {\n");
			for (ColumnMetaData column : columns) {
				this.writeIndent(segment, 2).append(
						"this." + column.getFieldName() + " = "
								+ column.getFieldName() + ";\n");
			}
			this.writeIndent(segment, 1).append("}\n\n");
		}

		// Add the constructor
		this.constructors.add(segment.toString());
	}

	/**
	 * Adds a field.
	 * 
	 * @param type
	 *            Type of the field.
	 * @param fieldName
	 *            Name of the field.
	 * @param documentation
	 *            Documentation of the field.
	 */
	public void addField(String type, String fieldName, String documentation) {
		// Ensure not a duplicate field
		if (this.fields.containsKey(fieldName)) {
			throw new IllegalStateException("Duplicate field names '"
					+ fieldName + "'");
		}

		// Add type to imports
		type = this.addImport(type);

		// Obtain the field declaration
		StringBuilder segment = new StringBuilder();
		this.writeDocumentation(segment, 1, documentation);
		this.writeIndent(segment, 1).append(
				"private " + type + " " + fieldName + ";\n\n");

		// Add the field declaration
		this.fields.put(fieldName, segment.toString());
	}

	/**
	 * Adds a property.
	 * 
	 * @param column
	 *            {@link ColumnMetaData}.
	 */
	public void addProperty(ColumnMetaData column) {

		// Add the field
		this.addField(column.getJavaType().getName(), column.getFieldName(),
				"Column " + column.getColumnName() + ".");

		// Obtain the type
		String type = this.addImport(column.getJavaType());

		StringBuilder segment = new StringBuilder();

		// Provide the property getter
		this.writeDocumentation(segment, 1, "Obtains value for column "
				+ column.getColumnName() + ".");
		this.writeIndent(segment, 1).append(
				"public " + type + " " + column.getGetMethodName() + "(){\n");
		this.writeIndent(segment, 2).append(
				"return this." + column.getFieldName() + ";\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Provide the property setter
		this.writeDocumentation(segment, 1, "Specifies value for column "
				+ column.getColumnName() + ".");
		this.writeIndent(segment, 1).append(
				"public void " + column.getSetMethodName() + "(" + type + " "
						+ column.getFieldName() + ") {\n");
		this.writeIndent(segment, 2).append(
				"this." + column.getFieldName() + " = " + column.getFieldName()
						+ ";\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Add the property declaration
		this.properties.put(column.getFieldName(), segment.toString());
	}

	/**
	 * Adds a linkTo.
	 * 
	 * @param classFields
	 *            Fields of this class linking to the table.
	 * @param linkedTable
	 *            Table being linked.
	 * @param linkedColumns
	 *            Columns linked to in the table.
	 */
	public void addLinkTo(ColumnMetaData[] classFields,
			TableMetaData linkedTable, ColumnMetaData[] linkedColumns) {

		// Add the table
		String tableType = this.addImport(linkedTable
				.getFullyQualifiedClassName());

		StringBuilder segment = new StringBuilder();

		// Provide the link to method
		this.writeDocumentation(segment, 1, "Links to {@link " + tableType
				+ "}.");
		this.writeIndent(segment, 1).append(
				"public void linkTo" + tableType + "(" + tableType
						+ " table) {\n");
		for (int i = 0; i < classFields.length; i++) {
			this.writeIndent(segment, 2).append(
					"this." + classFields[i].getFieldName() + " = table."
							+ linkedColumns[i].getGetMethodName() + "();\n");
		}
		this.writeIndent(segment, 1).append("}\n\n");

		// Add the linkTo declaration
		this.linkToTables.put(linkedTable.getFullyQualifiedClassName(), segment
				.toString());
	}

	/**
	 * Adds the load method.
	 * 
	 * @param table
	 *            {@link TableMetaData} specifying what to load.
	 */
	public void addLoad(TableMetaData table) {

		// Obtain the additional types
		String resultSetType = this.addImport(ResultSet.class);
		String sqlExceptionType = this.addImport(SQLException.class);

		StringBuilder segment = new StringBuilder();

		// Provide the load method
		this.writeDocumentation(segment, 1, "Loads state from the {@link "
				+ resultSetType + "}.");
		this.writeIndent(segment, 1).append(
				"public void load(" + resultSetType + " resultSet) throws "
						+ sqlExceptionType + " {\n");
		for (ColumnMetaData column : table.getColumns()) {
			String columnType = this.addImport(column.getJavaType());
			this.writeIndent(segment, 2).append(
					"this." + column.getFieldName() + " = (" + columnType
							+ ") resultSet.getObject(\""
							+ column.getColumnName() + "\");\n");
		}
		this.writeIndent(segment, 1).append("}\n\n");

		// Specify load method
		this.loadMethod = segment.toString();
	}

	/**
	 * Adds the retrieve method.
	 * 
	 * @param table
	 *            {@link TableMetaData} specifying what to retrieve.
	 */
	public void addRetrieve(TableMetaData table) {

		// Obtain the additional types
		String connectionType = this.addImport(Connection.class);
		String sqlExceptionType = this.addImport(SQLException.class);
		String preparedStatementType = this.addImport(PreparedStatement.class);
		String resultSetType = this.addImport(ResultSet.class);

		StringBuilder segment = new StringBuilder();

		// Obtain the method name
		String methodName = "retrieve";

		// Provide the retrieve methods
		this.writeDocumentation(segment, 1, "Retrieves a single {@link "
				+ table.getSimpleClassName() + "}.");
		this
				.writeIndent(segment, 1)
				.append(
						"public "
								+ table.getSimpleClassName()
								+ " "
								+ methodName
								+ "("
								+ connectionType
								+ " connection, String sql, Object... parameters) throws "
								+ sqlExceptionType + " {\n");
		this.writeIndent(segment, 2).append(
				"// Prepare statement for execution\n");
		this.writeIndent(segment, 2).append(
				preparedStatementType
						+ " statement = connection.prepareStatement(sql);\n");
		this.writeIndent(segment, 2).append("try {\n");
		this.writeIndent(segment, 3).append(
				"for (int i = 0; i < parameters.length; i++) {\n");
		this.writeIndent(segment, 4).append(
				"statement.setObject((i + 1), parameters[i]);\n");
		this.writeIndent(segment, 3).append("}\n");
		this.writeIndent(segment, 3).append("// Return result\n");
		this.writeIndent(segment, 3).append(
				resultSetType + " resultSet = statement.executeQuery();\n");
		this.writeIndent(segment, 3).append("if (resultSet.next()) {\n");
		this.writeIndent(segment, 4).append(
				table.getSimpleClassName() + " bean = new "
						+ table.getSimpleClassName() + "();\n");
		this.writeIndent(segment, 4).append("bean.load(resultSet);\n");
		this.writeIndent(segment, 4).append("return bean;\n");
		this.writeIndent(segment, 3).append("} else {\n");
		this.writeIndent(segment, 4).append("return null;\n");
		this.writeIndent(segment, 3).append("}\n");
		this.writeIndent(segment, 2).append("} finally {\n");
		this.writeIndent(segment, 3).append("statement.close();\n");
		this.writeIndent(segment, 2).append("}\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Specify the retrieve method
		this.retrieveMethods.put(methodName, segment.toString());
	}

	/**
	 * Adds the retrieve list method.
	 * 
	 * @param table
	 *            {@link TableMetaData} specifying what to retrieve.
	 */
	public void addRetrieveList(TableMetaData table) {

		// Obtain the additional types
		String listType = this.addImport(List.class);
		String linkedListType = this.addImport(LinkedList.class);
		String connectionType = this.addImport(Connection.class);
		String sqlExceptionType = this.addImport(SQLException.class);
		String preparedStatementType = this.addImport(PreparedStatement.class);
		String resultSetType = this.addImport(ResultSet.class);

		StringBuilder segment = new StringBuilder();

		// Obtain the method name
		String methodName = "retrieveList";

		// Provide the retrieve methods
		this.writeDocumentation(segment, 1, "Retrieves list of {@link "
				+ table.getSimpleClassName() + "} instances.");
		this
				.writeIndent(segment, 1)
				.append(
						"public "
								+ listType
								+ "<"
								+ table.getSimpleClassName()
								+ "> "
								+ methodName
								+ "("
								+ connectionType
								+ " connection, String sql, Object... parameters) throws "
								+ sqlExceptionType + " {\n");
		this.writeIndent(segment, 2).append(
				"// Prepare statement for execution\n");
		this.writeIndent(segment, 2).append(
				preparedStatementType
						+ " statement = connection.prepareStatement(sql);\n");
		this.writeIndent(segment, 2).append("try {\n");
		this.writeIndent(segment, 3).append(
				"for (int i = 0; i < parameters.length; i++) {\n");
		this.writeIndent(segment, 4).append(
				"statement.setObject((i + 1), parameters[i]);\n");
		this.writeIndent(segment, 3).append("}\n");
		this.writeIndent(segment, 3).append("// Return result\n");
		this.writeIndent(segment, 3).append(
				resultSetType + " resultSet = statement.executeQuery();\n");
		this.writeIndent(segment, 3).append(
				listType + "<" + table.getSimpleClassName() + "> beans = new "
						+ linkedListType + "<" + table.getSimpleClassName()
						+ ">();\n");
		this.writeIndent(segment, 3).append("while (resultSet.next()) {\n");
		this.writeIndent(segment, 4).append(
				table.getSimpleClassName() + " bean = new "
						+ table.getSimpleClassName() + "();\n");
		this.writeIndent(segment, 4).append("bean.load(resultSet);\n");
		this.writeIndent(segment, 4).append("beans.add(bean);\n");
		this.writeIndent(segment, 3).append("}\n");
		this.writeIndent(segment, 3).append("// Return results\n");
		this.writeIndent(segment, 3).append("return beans;\n");
		this.writeIndent(segment, 2).append("} finally {\n");
		this.writeIndent(segment, 3).append("statement.close();\n");
		this.writeIndent(segment, 2).append("}\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Specify the retrieve list method
		this.retrieveMethods.put(methodName, segment.toString());
	}

	/**
	 * Adds the retrieveBy method for the input {@link AccessMetaData}.
	 * 
	 * @param table
	 *            {@link TableMetaData} being accessed.
	 * @param access
	 *            {@link AccessMetaData}.
	 */
	public void addRetrieveBy(TableMetaData table, AccessMetaData access) {

		// Add the additional types
		String connectionType = this.addImport(Connection.class);
		String sqlExceptionType = this.addImport(SQLException.class);

		StringBuilder segment = new StringBuilder();

		// Obtain the method name
		StringBuilder methodName = new StringBuilder();
		methodName.append("retrieveBy");
		for (ColumnMetaData column : access.getColumns()) {
			methodName.append(FilingCabinetUtil.getSimpleClassName(column
					.getColumnName()));
		}

		// Provide the retrieve by method
		StringBuilder columnListing = new StringBuilder();
		boolean isFirst = true;
		for (ColumnMetaData column : access.getColumns()) {
			columnListing.append(isFirst ? "" : ", ");
			isFirst = false;
			columnListing.append(column.getColumnName());
		}
		this.writeDocumentation(segment, 1, "Retrieves {@link "
				+ table.getSimpleClassName() + "} by column "
				+ columnListing.toString() + ".");
		this.writeIndent(segment, 1).append("public ");
		if (access.isUnique()) {
			segment.append(table.getSimpleClassName());
		} else {
			segment.append("List<" + table.getSimpleClassName() + ">");
		}
		segment.append(" " + methodName.toString() + "(");
		if (access.getColumns().length == 1) {
			// Single column so use column type
			ColumnMetaData column = access.getColumns()[0];
			String columnType = this.addImport(column.getJavaType());
			segment.append(columnType + " " + column.getFieldName());
		} else {
			// Multiple columns so use index object
			segment.append(access.getSimpleClassName() + " access");
		}
		segment.append(", " + connectionType + " connection) throws "
				+ sqlExceptionType + " {\n");
		this.writeIndent(segment, 2).append(
				"return this.retrieve" + (access.isUnique() ? "" : "List")
						+ "(connection, \"SELECT * FROM "
						+ table.getTableName() + " WHERE ");
		isFirst = true;
		for (ColumnMetaData column : access.getColumns()) {
			segment.append(isFirst ? "" : " AND ");
			isFirst = false;
			segment.append(column.getColumnName() + " = ?");
		}
		segment.append("\"");
		if (access.getColumns().length == 1) {
			// Single column so pass in parameter
			segment.append(", " + access.getColumns()[0].getFieldName());
		} else {
			// Multiple columns so use properties on index object
			for (ColumnMetaData column : access.getColumns()) {
				segment.append(", access." + column.getGetMethodName() + "()");
			}
		}
		segment.append(");\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Add the retrieve by methods
		this.retrieveMethods.put(methodName.toString(), segment.toString());
	}

	/**
	 * Adds the retrieveFrom method for the input cross reference.
	 * 
	 * @param table
	 *            {@link TableMetaData} being accessed.
	 * @param columns
	 *            {@link ColumnMetaData} instances.
	 * @param linkTable
	 *            {@link TableMetaData} being linked.
	 * @param linkColumns
	 *            Linked {@link ColumnMetaData} instances.
	 */
	public void addRetrieveFrom(TableMetaData table, ColumnMetaData[] columns,
			TableMetaData linkTable, ColumnMetaData[] linkColumns) {

		// Add the additional types
		String connectionType = this.addImport(Connection.class);
		String sqlExceptionType = this.addImport(SQLException.class);
		String linkClassType = this.addImport(linkTable
				.getFullyQualifiedClassName());

		// Determine if access is unique
		AccessMetaData access = table.getAccess(columns);
		boolean isUnique = (access == null ? false : access.isUnique());

		StringBuilder segment = new StringBuilder();

		// Obtain the method name
		String methodName = "retrieveFrom" + linkTable.getSimpleClassName();

		// Provide the retrieve from method
		this.writeDocumentation(segment, 1, "Retrieves {@link "
				+ table.getSimpleClassName() + "} by {@link " + linkClassType
				+ "}.");
		this.writeIndent(segment, 1).append("public ");
		if (isUnique) {
			segment.append(table.getSimpleClassName());
		} else {
			segment.append("List<" + table.getSimpleClassName() + ">");
		}
		segment.append(" " + methodName + "(" + linkTable.getSimpleClassName()
				+ " table, " + connectionType + " connection) throws "
				+ sqlExceptionType + " {\n");
		this.writeIndent(segment, 2).append(
				"return this.retrieve" + (isUnique ? "" : "List")
						+ "(connection, \"SELECT * FROM "
						+ table.getTableName() + " WHERE ");
		boolean isFirst = true;
		for (ColumnMetaData column : columns) {
			segment.append(isFirst ? "" : " AND ");
			isFirst = false;
			segment.append(column.getColumnName() + " = ?");
		}
		segment.append("\"");
		for (ColumnMetaData linkColumn : linkColumns) {
			segment.append(", table." + linkColumn.getGetMethodName() + "()");
		}
		segment.append(");\n");
		this.writeIndent(segment, 1).append("}\n\n");

		// Add the retrieve by methods
		this.retrieveMethods.put(methodName, segment.toString());
	}

	/**
	 * Adds a retrieve method.
	 * 
	 * @param methodName
	 *            Name of method.
	 * @param methodDeclaration
	 *            Method declaration.
	 */
	protected void addRetrieveMethod(String methodName, String methodDeclaration) {
		// Ensure method name not already used
		if (this.retrieveMethods.containsKey(methodName)) {
			throw new IllegalStateException("Duplicate retrieve method name '"
					+ methodName + "'");
		}

		// Add the retrieve method
		this.retrieveMethods.put(methodName, methodDeclaration);
	}

	/**
	 * Generates the class java code.
	 * 
	 * @return Class java code.
	 */
	public String generate() {

		// Create the java code buffer
		StringBuilder code = new StringBuilder();

		// Add the package declaration
		if (this.packageName != null) {
			code.append("package " + this.packageName + ";\n\n");
		}

		// Write the imports
		this.writeImports(code);

		// Write the class declarations
		this.writeClassDeclaration(code);

		// Write the fields
		for (String fieldName : this.sort(this.fields.keySet())) {
			String fieldDeclaration = this.fields.get(fieldName);
			code.append(fieldDeclaration);
		}

		// Write the constructors
		for (String constructor : this.sort(this.constructors)) {
			code.append(constructor);
		}

		// Write the properties
		for (String propertyName : this.sort(this.properties.keySet())) {
			String propertyDeclaration = this.properties.get(propertyName);
			code.append(propertyDeclaration);
		}

		// Write the linkTo methods
		for (String tableClassName : this.sort(this.linkToTables.keySet())) {
			String linkToDeclaration = this.linkToTables.get(tableClassName);
			code.append(linkToDeclaration);
		}

		// Write the load method
		code.append(this.loadMethod);

		// Write the retrieve methods
		for (String retrieveMethodName : this.sort(this.retrieveMethods
				.keySet())) {
			String retrieveMethodDeclaration = this.retrieveMethods
					.get(retrieveMethodName);
			code.append(retrieveMethodDeclaration);
		}

		// Close the class
		code.append("}\n");

		// Return the code
		return code.toString();
	}

	/**
	 * Returns the input items sorted.
	 * 
	 * @param items
	 *            Items to be sorted.
	 * @return Sorted items.
	 */
	private List<String> sort(Collection<String> items) {

		// Create new list of items (to be immutable)
		List<String> sortedItems = new ArrayList<String>(items.size());
		sortedItems.addAll(items);

		// Sort the items
		Collections.sort(sortedItems);

		// Move particular items to end
		for (String item : new String[] { "retrieve", "retrieveList" }) {
			if (sortedItems.contains(item)) {
				sortedItems.remove(item);
				sortedItems.add(item);
			}
		}

		// Return the sorted list
		return sortedItems;
	}

	/**
	 * Writes the imports.
	 * 
	 * @param code
	 *            Buffer of generated code.
	 */
	protected void writeImports(StringBuilder code) {
		boolean isImportedProvided = false;
		for (String importedClassName : this.sort(this.importedClasses)) {

			// Need not import java.lang classes
			if (importedClassName.startsWith("java.lang.")) {
				continue;
			}

			// Import the class
			code.append("import " + importedClassName + ";\n");
			isImportedProvided = true;
		}
		if (isImportedProvided) {
			code.append("\n");
		}
	}

	/**
	 * Writes the class declaration.
	 * 
	 * @param code
	 *            Buffer of generated code.
	 */
	protected void writeClassDeclaration(StringBuilder code) {
		code.append("public class " + className + " ");
		if (this.extendsClassName != null) {
			code.append("extends " + this.extendsClassName + " ");
		}
		if (this.implementingInterfaceNames.size() > 0) {
			code.append("implements ");
			boolean isFirst = true;
			for (String interfaceName : this.implementingInterfaceNames) {
				code.append(isFirst ? "" : ", ");
				isFirst = false;
				code.append(interfaceName);
			}
		}
		code.append("{\n\n");
	}

	/**
	 * Writes the documentation.
	 * 
	 * @param code
	 *            Buffer of generated code.
	 * @param indent
	 *            Number of indents.
	 * @param documentation
	 *            Documentation. May be <code>null</code>.
	 */
	protected void writeDocumentation(StringBuilder code, int indent,
			String documentation) {
		// Ensure have documentation
		if (documentation == null) {
			return;
		}

		// Provide documentation
		String[] docLines = documentation.split("\n");
		this.writeIndent(code, indent);
		code.append("/**\n");
		for (String docLine : docLines) {
			this.writeIndent(code, indent);
			code.append(" * " + docLine + "\n");
		}
		this.writeIndent(code, indent);
		code.append(" */\n");
	}

	/**
	 * Writes the indent.
	 * 
	 * @param code
	 *            Buffer of generated code.
	 * @param indent
	 *            Number of indents.
	 * @return Input {@link StringBuilder}.
	 */
	protected StringBuilder writeIndent(StringBuilder code, int indent) {
		for (int i = 0; i < indent; i++) {
			code.append("    ");
		}
		return code;
	}

}
