package net.officefloor.javascript;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.script.AbstractScriptProcedureSourceServiceFactory;
import net.officefloor.script.ScriptExceptionTranslator;

/**
 * JavaScript function {@link ProcedureSourceServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptProcedureSourceServiceFactory extends AbstractScriptProcedureSourceServiceFactory {

	@Override
	protected String getSourceName() {
		return "JavaScript";
	}

	@Override
	protected String[] getScriptFileExtensions(SourceContext context) throws Exception {
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
	protected String getMetaDataScriptPath(SourceContext context) throws Exception {
		return this.getClass().getPackage().getName().replace('.', '/') + "/OfficeFloorFunctionMetaData.js";
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