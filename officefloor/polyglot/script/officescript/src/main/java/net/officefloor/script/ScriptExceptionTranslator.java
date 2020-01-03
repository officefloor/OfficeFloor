package net.officefloor.script;

import javax.script.ScriptException;

import net.officefloor.server.http.HttpException;

/**
 * <p>
 * Translate {@link ScriptException} to possible more appropriate
 * {@link Throwable}.
 * <p>
 * For example, the {@link HttpException} may be wrapped in the
 * {@link ScriptException} and this provides means to extract and throw.
 * 
 * @author Daniel Sagenschneider
 */
public interface ScriptExceptionTranslator {

	/**
	 * Translates the {@link ScriptException}.
	 * 
	 * @param scriptException {@link ScriptException}.
	 * @return Translated {@link Throwable} or <code>null</code> to throw original
	 *         {@link ScriptException}.
	 */
	Throwable translate(ScriptException scriptException);

}