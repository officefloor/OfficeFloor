package net.officefloor.activity.source;

import java.sql.SQLException;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Example {@link Procedure} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleProcedure {

	/**
	 * Indicates if the procedure was run.
	 */
	public static boolean isProcedureRun = false;

	/**
	 * Result.
	 */
	public static String result = null;

	/**
	 * {@link SQLException}.
	 */
	public static SQLException failure = null;

	/*
	 * ============ Procedures =================
	 */

	public void procedure() {
		isProcedureRun = true;
	}

	public String passThrough(@Parameter String value) {
		return value;
	}

	public void result(@Parameter String value) {
		result = value;
	}

	public void injectObject(String value) {
		result = value;
	}

	public void propagate(@Parameter SQLException value) throws SQLException {
		throw value;
	}

	public void handleEscalation(@Parameter SQLException value) {
		failure = value;
	}

}