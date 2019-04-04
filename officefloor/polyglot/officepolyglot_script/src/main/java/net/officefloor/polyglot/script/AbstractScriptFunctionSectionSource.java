/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.script;

import javax.script.ScriptEngine;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.section.clazz.AbstractFunctionSectionSource;

/**
 * Abstract script function {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractScriptFunctionSectionSource extends AbstractFunctionSectionSource {

	/**
	 * {@link Property} name for the {@link ScriptEngine} name.
	 */
	public static final String PROPERTY_ENGINE_NAME = ScriptManagedFunctionSource.PROPERTY_ENGINE_NAME;

	/**
	 * {@link Property} name for the Script function name.
	 */
	public static final String PROPERTY_FUNCTION_NAME = ScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME;

	/**
	 * Name of the {@link ScriptEngine}.
	 */
	private String engineName;

	/**
	 * Path to the setup script. May be <code>null</code> for no setup.
	 */
	private String setupScriptPath;

	/**
	 * Path to the script file.
	 */
	private String scriptPath;

	/**
	 * Path to the script that extracts the function meta-data.
	 */
	private String metaDataScriptPath;

	/**
	 * Name of the function.
	 */
	private String functionName;

	/**
	 * Obtains the name of the {@link ScriptEngine}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Name of the {@link ScriptEngine}.
	 * @throws Exception If fails to obtain the name of the {@link ScriptEngine}.
	 */
	protected abstract String getScriptEngineName(SourceContext context) throws Exception;

	/**
	 * Obtains the path to the setup script.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the setup script or <code>null</code> if no setup.
	 * @throws Exception If fails to obtain the setup script path.
	 */
	protected String getSetupScriptPath(SourceContext context) throws Exception {
		return null;
	}

	/**
	 * Obtains the path to the script that extracts the function meta-data.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the script that extracts the function meta-data.
	 * @throws Exception If fails to obtain the path to the function meta-data
	 *                   script.
	 */
	protected abstract String getMetaDataScriptPath(SourceContext context) throws Exception;

	/**
	 * Obtains the {@link ScriptExceptionTranslator}.
	 * 
	 * @return {@link ScriptExceptionTranslator}.
	 */
	protected ScriptExceptionTranslator getScriptExceptionTranslator() {
		return null; // will be defaulted
	}

	/*
	 * ================= ClassSectionSource =====================
	 */

	@Override
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {
		this.scriptPath = sectionClassName;
		this.engineName = this.getScriptEngineName(this.getContext());
		this.setupScriptPath = this.getSetupScriptPath(this.getContext());
		this.functionName = this.getContext().getProperty(PROPERTY_FUNCTION_NAME);
		this.metaDataScriptPath = this.getMetaDataScriptPath(this.getContext());
		return ScriptSection.class;
	}

	/**
	 * Script section {@link Class}.
	 */
	protected static class ScriptSection {
		public void script() {
		}
	}

	@Override
	protected ObjectForMethod loadObjectForMethod(Class<?> sectionClass) throws Exception {
		return null;
	}

	@Override
	protected FunctionNamespaceType loadFunctionNamespaceType(String namespace, Class<?> sectionClass) {
		PropertyList properties = this.getContext().createPropertyList();
		properties.addProperty(ScriptManagedFunctionSource.PROPERTY_ENGINE_NAME).setValue(this.engineName);
		if (this.setupScriptPath != null) {
			properties.addProperty(ScriptManagedFunctionSource.PROPERTY_SETUP_SCRIPT_PATH)
					.setValue(this.setupScriptPath);
		}
		properties.addProperty(ScriptManagedFunctionSource.PROPERTY_SCRIPT_PATH).setValue(this.scriptPath);
		properties.addProperty(ScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME).setValue(this.functionName);
		properties.addProperty(ScriptManagedFunctionSource.PROPERTY_METADATA_SCRIPT_PATH)
				.setValue(this.metaDataScriptPath);
		return this.getContext().loadManagedFunctionType(namespace, ScriptManagedFunctionSource.class.getName(),
				properties);
	}

	@Override
	protected SectionFunctionNamespace adddSectionFunctionNamespace(String namespace, Class<?> sectionClass) {

		// Obtain the script engine translator
		ScriptExceptionTranslator translator = this.getScriptExceptionTranslator();

		// Configure the function
		SectionFunctionNamespace functionNamespace = this.getDesigner().addSectionFunctionNamespace(namespace,
				new ScriptManagedFunctionSource(translator));
		functionNamespace.addProperty(ScriptManagedFunctionSource.PROPERTY_ENGINE_NAME, this.engineName);
		if (this.setupScriptPath != null) {
			functionNamespace.addProperty(ScriptManagedFunctionSource.PROPERTY_SETUP_SCRIPT_PATH, this.setupScriptPath);
		}
		functionNamespace.addProperty(ScriptManagedFunctionSource.PROPERTY_SCRIPT_PATH, this.scriptPath);
		functionNamespace.addProperty(ScriptManagedFunctionSource.PROPERTY_FUNCTION_NAME, this.functionName);
		functionNamespace.addProperty(ScriptManagedFunctionSource.PROPERTY_METADATA_SCRIPT_PATH,
				this.metaDataScriptPath);
		return functionNamespace;
	}

}