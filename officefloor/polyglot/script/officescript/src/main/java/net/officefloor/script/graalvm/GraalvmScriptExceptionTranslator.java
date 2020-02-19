package net.officefloor.script.graalvm;

import javax.script.ScriptException;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import net.officefloor.script.ScriptExceptionTranslator;

/**
 * {@link ScriptExceptionTranslator} for the GraalVM.
 * 
 * @author Daniel Sagenschneider
 */
public class GraalvmScriptExceptionTranslator implements ScriptExceptionTranslator {

	/*
	 * ================= ScriptExceptionTranslator ==================
	 */

	@Override
	public Throwable translate(ScriptException scriptException) {
		if (scriptException.getCause() instanceof PolyglotException) {
			PolyglotException polyglotEx = (PolyglotException) scriptException.getCause();
			if (polyglotEx.isGuestException()) {
				Object guestObject = polyglotEx.getGuestObject();
				if (guestObject instanceof Value) {
					Value value = (Value) guestObject;
					Object cause = value.asHostObject();
					if (cause instanceof Throwable) {
						return (Throwable) cause;
					}
				}
			} else if (polyglotEx.isHostException()) {
				return polyglotEx.asHostException();
			}
		}
		return scriptException;
	}

}