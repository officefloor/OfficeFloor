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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.SourceContext;

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
		return (ex) -> {
			if (ex.getCause() instanceof PolyglotException) {
				PolyglotException polyglotEx = (PolyglotException) ex.getCause();
				if (polyglotEx.isGuestException()) {
					Object guestObject = polyglotEx.getGuestObject();
					if (guestObject instanceof Value) {
						Value value = (Value) guestObject;
						Object cause = value.asHostObject();
						if (cause instanceof Throwable) {
							return (Throwable) cause;
						}
					}
				}
			}
			return ex;
		};
	}

}