/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.script.graalvm.GraalvmScriptExceptionTranslator;

/**
 * Mock {@link ProcedureSourceServiceFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockScriptProcedureSourceServiceFactory extends AbstractScriptProcedureSourceServiceFactory {

	/*
	 * ============= AbstractScriptProcedureSourceServiceFactory =============
	 */

	@Override
	protected String getSourceName() {
		return "MockScript";
	}

	@Override
	protected String[] getScriptFileExtensions(SourceContext context) {
		return new String[] { "js" };
	}

	@Override
	protected String getScriptEngineName(SourceContext context) throws Exception {
		return "graal.js";
	}

	@Override
	protected void decorateScriptEngine(ScriptEngine engine, SourceContext context) throws Exception {
		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("polyglot.js.allowAllAccess", true);
	}

	@Override
	protected String getSetupScriptPath(SourceContext context) throws Exception {
		return "javascript/Setup.js";
	}

	@Override
	protected String getMetaDataScriptPath(SourceContext context) throws Exception {
		return "javascript/OfficeFloorFunctionMetaData.js";
	}

	@Override
	protected ScriptExceptionTranslator getScriptExceptionTranslator() {
		return new GraalvmScriptExceptionTranslator();
	}

}
