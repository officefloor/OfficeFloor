/*-
 * #%L
 * Model Generator
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.model.generate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.generate.model.AbstractPropertyMetaData;
import net.officefloor.model.generate.model.FieldMetaData;
import net.officefloor.model.generate.model.ListMetaData;
import net.officefloor.model.generate.model.ModelMetaData;

/**
 * Generates the Model.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelGenerator {

	/**
	 * Meta-data of the Model.
	 */
	private final ModelMetaData metaData;

	/**
	 * Generic meta-data for creating the Model.
	 */
	private final GraphNodeMetaData graphNode;

	/**
	 * Writer to output the model.
	 */
	private PrintWriter writer;

	/**
	 * Initiate.
	 * 
	 * @param metaData  Meta-data of the Model.
	 * @param graphNode {@link GraphNodeMetaData} for creating the Model.
	 */
	public ModelGenerator(ModelMetaData metaData, GraphNodeMetaData graphNode) {
		this.metaData = metaData;
		this.graphNode = graphNode;
	}

	/**
	 * Generates the Model.
	 * 
	 * @param context Context to create the Model.
	 * @return {@link ModelFile} for the Model.
	 * @throws Exception If fail.
	 */
	public synchronized ModelFile generateModel(ModelContext context) throws Exception {

		// Create the writer to output contents
		ByteArrayOutputStream marshalledModel = new ByteArrayOutputStream();
		this.writer = new PrintWriter(new OutputStreamWriter(marshalledModel));

		// Write the model
		this.header();
		writeLine();
		this.imports();
		writeLine();
		this.classDefinition();

		// Flush the model
		this.writer.flush();

		// Create the configuration item
		return context.createModelFile(
				this.metaData.getPackageName().replace('.', '/') + "/" + this.metaData.getClassName() + ".java",
				new ByteArrayInputStream(marshalledModel.toByteArray()));
	}

	/**
	 * Indicates if object has fields or list state.
	 */
	private boolean isStateForObject() {
		return (this.metaData.getFields().size() > 0) || (this.metaData.getLists().size() > 0);
	}

	/**
	 * Header.
	 */
	private void header() throws Exception {
		writeLine("/*");
		writeLine(" * " + this.graphNode.getLicense());
		writeLine(" */");
		writeLine("package " + this.metaData.getPackageName() + ";");
	}

	/**
	 * Imports.
	 */
	private void imports() throws Exception {
		// Only import lists if have lists
		if (this.metaData.getLists().size() > 0) {
			writeLine("import java.util.List;");
			writeLine("import java.util.LinkedList;");
			writeLine();
		}
		// Write extra imports
		for (String importClass : this.metaData.getImportClasses()) {
			writeLine("import " + importClass + ";");
		}
		// Write necessary extension
		writeLine("import " + GeneratedAnnotationJavaFacet.getGeneratedClassName() + ";");
		writeLine();
		writeLine("import net.officefloor.model.AbstractModel;");
		writeLine("import net.officefloor.model.ConnectionModel;");
		if (!this.metaData.isConnectionModel()) {
			writeLine("import net.officefloor.model.ItemModel;");
			writeLine("import net.officefloor.model.RemoveConnectionsAction;");
		}
	}

	/**
	 * Class definition.
	 */
	private void classDefinition() throws Exception {

		// Provide suppress warning on ItemModel if required
		String classSuffix = this.metaData.getClassSuffix();
		if (!this.metaData.isConnectionModel()) {
			if ((classSuffix != null) && (classSuffix.trim().length() > 0)) {
				// Generic model so suppress 'unchecked'
				writeLine("@SuppressWarnings(\"unchecked\")");
			}
		}

		// Provide generated annotation
		writeLine("@Generated(\"" + this.getClass().getName() + "\")");

		// Class signature
		StringBuilder signature = new StringBuilder();
		signature.append("public class " + this.metaData.getClassName() + this.metaData.getClassSuffix()
				+ " extends AbstractModel implements " + (this.metaData.isConnectionModel() ? "ConnectionModel"
						: "ItemModel<" + this.metaData.getClassName() + ">"));
		for (String interfaceName : this.metaData.getInterfaces()) {
			signature.append(", " + interfaceName);
		}
		signature.append(" {");
		writeLine(signature.toString());

		// Write class contents
		writeLine();
		this.events();
		writeLine();
		this.defaultConstructor();
		writeLine();
		if (this.nonLinkedConstructor(false)) {
			writeLine();
		}
		if (this.nonLinkedConstructor(true)) {
			writeLine();
		}
		if (this.isStateForObject()) {
			// Have state so provide convenience
			this.fullConstructor(false);
			writeLine();
		}
		this.fullConstructor(true);
		writeLine();
		this.fields();
		this.lists();
		writeLine();
		if (this.metaData.isConnectionModel()) {
			this.connectionMethods();
		} else {
			this.removeConnectionMethod();
		}

		// Close class
		writeLine("}");
	}

	/**
	 * Events.
	 */
	@SuppressWarnings("unchecked")
	private void events() throws Exception {
		writeLine("    public static enum " + this.metaData.getEventName() + " {");
		write("    ");
		writeListing(",", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				write(" CHANGE_" + field.getCapitalisedName());
			}

			protected void writeList(ListMetaData list) {
				write(" ADD_" + list.getCapitalisedName() + ", REMOVE_" + list.getCapitalisedName());
			}
		}, this.metaData.getFields(), this.metaData.getLists());
		writeLine();
		writeLine("    }");
	}

	/**
	 * Default constructor.
	 */
	private void defaultConstructor() throws Exception {
		writeLine("    /**");
		writeLine("     * Default constructor.");
		writeLine("     */");
		writeLine("    public " + this.metaData.getClassName() + "() {");
		writeLine("    }");
	}

	/**
	 * Convenience constructor for a new non-linked instance.
	 * 
	 * @param isIncludeCoordinates Indicates if to include X/Y co-ordinates.
	 * @return <code>true</code> if constructor written.
	 */
	@SuppressWarnings("unchecked")
	private boolean nonLinkedConstructor(boolean isIncludeCoordinates) throws Exception {

		final String MODEL_TYPE_SUFFIX = "Model";

		// Determine if links
		boolean isLinks = false;
		boolean isOnlyLinks = true;
		for (FieldMetaData field : this.metaData.getFields()) {
			if (field.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				isLinks = true;
			} else {
				isOnlyLinks = false;
			}
		}
		for (ListMetaData list : this.metaData.getLists()) {
			if (list.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				isLinks = true;
			} else {
				isOnlyLinks = false;
			}
		}

		// Determine if not linked
		if (!isLinks) {
			return false; // not linked (already constructor written)
		}

		// Determine if all links
		if (isOnlyLinks) {
			return false; // default constructor (already written)
		}

		// Write the non-linked construct
		writeLine("    /**");
		writeLine("     * Convenience constructor for new non-linked instance"
				+ (isIncludeCoordinates ? " allowing XY initialising" : "") + ".");
		writeLine("     *");

		// Parameters
		for (FieldMetaData field : this.metaData.getFields()) {
			if (!field.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				writeLine("     * @param " + field.getPropertyName() + " " + field.getDescription());
			}
		}
		for (ListMetaData list : this.metaData.getLists()) {
			if (!list.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				writeLine("     * @param " + list.getPropertyName() + " " + list.getDescription());
			}
		}
		if (isIncludeCoordinates) {
			writeLine("     * @param x Horizontal location.");
			writeLine("     * @param y Vertical location.");
		}
		writeLine("     */");
		writeLine("    public " + this.metaData.getClassName() + "(");

		// Parameters
		boolean isFirst = true;
		for (FieldMetaData field : this.metaData.getFields()) {
			if (!field.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				if (isFirst) {
					write("      ");
					isFirst = false;
				} else {
					write("    , ");
				}
				writeLine(field.getType() + " " + field.getPropertyName());
			}
		}
		for (ListMetaData list : this.metaData.getLists()) {
			if (!list.getType().endsWith(MODEL_TYPE_SUFFIX)) {
				if (isFirst) {
					write("      ");
					isFirst = false;
				} else {
					write("    , ");
				}
				writeLine(list.getType() + "[] " + list.getPropertyName());
			}
		}

		if (isIncludeCoordinates) {
			if (this.isStateForObject()) {
				writeLine("    , int x");
			} else {
				writeLine("      int x");
			}
			writeLine("    , int y");
		}

		writeLine("    ) {");

		// Specify values
		writeListing("", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				if (!field.getType().endsWith(MODEL_TYPE_SUFFIX)) {
					writeLine("        this." + field.getPropertyName() + " = " + field.getPropertyName() + ";");
				}
			}

			protected void writeList(ListMetaData list) {
				if (!list.getType().endsWith(MODEL_TYPE_SUFFIX)) {
					writeLine("        if (" + list.getPropertyName() + " != null) {");
					writeLine("            for (" + list.getType() + " model : " + list.getPropertyName() + ") {");
					writeLine("                this." + list.getPropertyName() + ".add(model);");
					writeLine("            }");
					writeLine("        }");
				}
			}
		}, this.metaData.getFields(), this.metaData.getLists());

		if (isIncludeCoordinates) {
			writeLine("        this.setX(x);");
			writeLine("        this.setY(y);");
		}

		writeLine("    }");

		// Return indicating constructor written
		return true;
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param isIncludeCoordinates Indicates if to include X/Y co-ordinates.
	 */
	@SuppressWarnings("unchecked")
	private void fullConstructor(boolean isIncludeCoordinates) throws Exception {
		boolean isFieldsLists = ((this.metaData.getFields().size() > 0) || (this.metaData.getLists().size() > 0));
		writeLine("    /**");
		writeLine("     * Convenience constructor" + (isIncludeCoordinates ? " allowing XY initialising" : "") + ".");
		writeLine("     *");
		if (isFieldsLists) {
			write("     * ");
		}
		writeListing("     * ", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				writeLine("@param " + field.getPropertyName() + " " + field.getDescription());
			}

			protected void writeList(ListMetaData list) {
				writeLine("@param " + list.getPropertyName() + " " + list.getDescription());
			}
		}, this.metaData.getFields(), this.metaData.getLists());
		if (isIncludeCoordinates) {
			writeLine("     * @param x Horizontal location.");
			writeLine("     * @param y Vertical location.");
		}
		writeLine("     */");
		writeLine("    public " + this.metaData.getClassName() + "(");

		// Parameters
		if (isFieldsLists) {
			write("      ");
		}
		writeListing("    , ", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				writeLine(field.getType() + " " + field.getPropertyName());
			}

			protected void writeList(ListMetaData list) {
				writeLine(list.getType() + "[] " + list.getPropertyName());
			}
		}, this.metaData.getFields(), this.metaData.getLists());

		if (isIncludeCoordinates) {
			if (this.isStateForObject()) {
				writeLine("    , int x");
			} else {
				writeLine("      int x");
			}
			writeLine("    , int y");
		}

		writeLine("    ) {");

		// Specify values
		writeListing("", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				writeLine("        this." + field.getPropertyName() + " = " + field.getPropertyName() + ";");
			}

			protected void writeList(ListMetaData list) {
				writeLine("        if (" + list.getPropertyName() + " != null) {");
				writeLine("            for (" + list.getType() + " model : " + list.getPropertyName() + ") {");
				writeLine("                this." + list.getPropertyName() + ".add(model);");
				writeLine("            }");
				writeLine("        }");
			}
		}, this.metaData.getFields(), this.metaData.getLists());

		if (isIncludeCoordinates) {
			writeLine("        this.setX(x);");
			writeLine("        this.setY(y);");
		}

		writeLine("    }");
	}

	/**
	 * Fields.
	 */
	@SuppressWarnings("unchecked")
	private void fields() throws Exception {
		writeListing("", new WriteAction() {
			protected void writeField(FieldMetaData field) {

				// Blank line separator
				writeLine();

				// Variable
				writeLine("    /**");
				writeLine("     * " + field.getDescription());
				writeLine("     */");
				writeLine("    private " + field.getType() + " " + field.getPropertyName() + ";");
				writeLine();

				// Accessor
				writeLine("    /**");
				writeLine("     * @return " + field.getDescription());
				writeLine("     */");
				writeLine("    public " + field.getType() + " get" + field.getCamelCaseName() + "() {");
				writeLine("        return this." + field.getPropertyName() + ";");
				writeLine("    }");
				writeLine();

				// Mutator
				writeLine("    /**");
				writeLine("     * @param " + field.getPropertyName() + " " + field.getDescription());
				writeLine("     */");
				writeLine("    public void set" + field.getCamelCaseName() + "(" + field.getType() + " "
						+ field.getPropertyName() + ") {");
				writeLine("        " + field.getType() + " oldValue = this." + field.getPropertyName() + ";");
				writeLine("        this." + field.getPropertyName() + " = " + field.getPropertyName() + ";");
				writeLine("        this.changeField(oldValue, this." + field.getPropertyName() + ", "
						+ ModelGenerator.this.metaData.getEventName() + ".CHANGE_" + field.getCapitalisedName() + ");");
				writeLine("    }");
				writeLine();
			}
		}, this.metaData.getFields());
	}

	/**
	 * Lists.
	 */
	@SuppressWarnings("unchecked")
	private void lists() throws Exception {
		writeListing("", new WriteAction() {
			protected void writeList(ListMetaData list) {

				// Blank line separator
				writeLine();

				// Variable
				writeLine("    /**");
				writeLine("     * " + list.getDescription());
				writeLine("     */");
				writeLine("    private List<" + list.getType() + "> " + list.getPropertyName() + " = new LinkedList<"
						+ list.getType() + ">();");
				writeLine();

				// Accessor
				writeLine("    /**");
				writeLine("     * @return " + list.getDescription());
				writeLine("     */");
				writeLine("    public List<" + list.getType() + "> get" + list.getPluralName() + "() {");
				writeLine("        return this." + list.getPropertyName() + ";");
				writeLine("    }");
				writeLine();

				// Add method
				writeLine("    /**");
				writeLine("     * @param " + list.getPropertyName() + " " + list.getDescription());
				writeLine("     */");
				writeLine("    public void add" + list.getCamelCaseName() + "(" + list.getType() + " "
						+ list.getPropertyName() + ") {");
				writeLine("        this.addItemToList(" + list.getPropertyName() + ", this." + list.getPropertyName()
						+ ", " + ModelGenerator.this.metaData.getEventName() + ".ADD_" + list.getCapitalisedName()
						+ ");");
				writeLine("    }");
				writeLine();

				// Remove method
				writeLine("    /**");
				writeLine("     * @param " + list.getPropertyName() + " " + list.getDescription());
				writeLine("     */");
				writeLine("    public void remove" + list.getCamelCaseName() + "(" + list.getType() + " "
						+ list.getPropertyName() + ") {");
				writeLine("        this.removeItemFromList(" + list.getPropertyName() + ", this."
						+ list.getPropertyName() + ", " + ModelGenerator.this.metaData.getEventName() + ".REMOVE_"
						+ list.getCapitalisedName() + ");");
				writeLine("    }");
				writeLine();
			}
		}, this.metaData.getLists());
	}

	/**
	 * Connection methods.
	 */
	@SuppressWarnings("unchecked")
	private void connectionMethods() throws Exception {
		// Is remove
		writeLine("    /**");
		writeLine("     * @return Indicates if removable.");
		writeLine("     */");
		writeLine("    public boolean isRemovable() {");
		writeLine("        return true;");
		writeLine("    }");
		writeLine();

		// Connect
		writeLine("    /**");
		writeLine("     * Connects to the {@link AbstractModel} instances.");
		writeLine("     */");
		writeLine("    public void connect() {");
		writeListing("", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				String endPointConnect = field.getEndPointConnect();
				if (endPointConnect != null) {
					writeLine("        this." + field.getPropertyName() + "." + endPointConnect + ";");
				}
			}
		}, this.metaData.getFields());
		writeLine("    }");
		writeLine();

		// Remove
		writeLine("    /**");
		writeLine("     * Removes connection to the {@link AbstractModel} instances.");
		writeLine("     */");
		writeLine("    public void remove() {");
		writeListing("", new WriteAction() {
			protected void writeField(FieldMetaData field) {
				String endPointRemove = field.getEndPointRemove();
				if (endPointRemove != null) {
					writeLine("        this." + field.getPropertyName() + "." + endPointRemove + ";");
				}
			}
		}, this.metaData.getFields());
		writeLine("    }");
		writeLine();
	}

	/**
	 * Remove connection method.
	 * 
	 * @throws Exception If fails to create the remove connection {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void removeConnectionMethod() throws Exception {
		// Method signature
		writeLine("    /**");
		writeLine("     * Remove Connections.");
		writeLine("     *");
		writeLine("     * @return {@link RemoveConnectionsAction} to remove the {@link ConnectionModel} instances.");
		writeLine("     */");
		writeLine("    public RemoveConnectionsAction<" + this.metaData.getClassName() + "> removeConnections() {");

		// Create the action for return
		writeLine("        RemoveConnectionsAction<" + this.metaData.getClassName()
				+ "> _action = new RemoveConnectionsAction<" + this.metaData.getClassName() + ">(this);");

		// Create the listing of properties
		List<AbstractPropertyMetaData> allProperties = new LinkedList<AbstractPropertyMetaData>();
		allProperties.addAll(this.metaData.getFields());
		allProperties.addAll(this.metaData.getLists());

		// Disconnect connections
		this.writeListing("", new WriteAction() {
			@Override
			protected void writeProperty(AbstractPropertyMetaData property) throws Exception {
				// Obtain the type meta-data
				ModelMetaData typeMetaData = ModelGenerator.this.graphNode.getModelMetaData(property.getType());
				if (typeMetaData != null) {
					// Remove connection
					if (typeMetaData.isConnectionModel()) {
						writeLine("        _action.disconnect(this." + property.getPropertyName() + ");");
					}
				}
			}
		}, allProperties);

		// Cascade remove connections
		this.writeListing("", new WriteAction() {

			@Override
			protected void writeField(FieldMetaData field) throws Exception {
				if (field.isCascadeRemove()) {
					writeLine("        if (this." + field.getPropertyName() + " != null) {");
					writeLine("            _action.addCascadeModel(this." + field.getPropertyName()
							+ ".removeConnections());");
					writeLine("        }");
				}
			}

			@Override
			protected void writeList(ListMetaData list) throws Exception {
				if (list.isCascadeRemove()) {
					writeLine("        for (" + list.getType() + " _cascade : this." + list.getPropertyName() + ") {");
					writeLine("            _action.addCascadeModel(_cascade.removeConnections());");
					writeLine("        }");
				}
			}
		}, allProperties);

		// Return and close method
		writeLine("        return _action;");
		writeLine("    }");
	}

	/*
	 * ========================================================================
	 * Write methods
	 * ========================================================================
	 */

	/**
	 * Writes the listing of objects.
	 */
	@SuppressWarnings("unchecked")
	private void writeListing(String separator, WriteAction action,
			List<? extends AbstractPropertyMetaData>... properties) throws Exception {

		// Flag first
		boolean isFirst = true;

		// Write the listing
		for (List<? extends AbstractPropertyMetaData> propertyListing : properties) {
			// Write the properties
			for (AbstractPropertyMetaData property : propertyListing) {
				if (!isFirst) {
					write(separator);
				}
				action.writeProperty(property);
				isFirst = false;
			}
		}
	}

	/**
	 * Action to write the particular entry.
	 */
	private abstract class WriteAction {

		/**
		 * Override to write the property.
		 * 
		 * @param property Property.
		 * @throws Exception If fails.
		 */
		protected void writeProperty(AbstractPropertyMetaData property) throws Exception {
			if (property instanceof FieldMetaData) {
				writeField((FieldMetaData) property);
			} else if (property instanceof ListMetaData) {
				writeList((ListMetaData) property);
			} else {
				throw new UnsupportedOperationException("Unkown type " + property.getClass().getName());
			}
		}

		/**
		 * Override to write the field.
		 * 
		 * @param field Field.
		 * @throws Exception If fails.
		 */
		protected void writeField(FieldMetaData field) throws Exception {
		}

		/**
		 * Override to write the list.
		 * 
		 * @param list List.
		 * @throws Exception If fails.
		 */
		protected void writeList(ListMetaData list) throws Exception {
		}
	}

	/**
	 * Moves to the next line.
	 */
	private void writeLine() {
		this.writer.println();
	}

	/**
	 * Writes the text followed by a end of line.
	 * 
	 * @param text Text.
	 */
	private void writeLine(String text) {
		this.writer.println(text);
	}

	/**
	 * Writes the text only.
	 * 
	 * @param text Text.
	 */
	private void write(String text) {
		this.writer.print(text);
	}

}
