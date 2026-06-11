package net.officefloor.tutorial.springrestgovernance;

import net.officefloor.plugin.governance.clazz.Disregard;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

// START SNIPPET: tutorial
/**
 * Class-based governance loaded from {@code officefloor/govern/audit.yml}.
 *
 * <p>OfficeFloor infers the extension interface ({@link Auditable}) from the
 * parameter type of the {@link Govern @Govern} method.  Any managed object
 * implementing {@link Auditable} in a governed function is enrolled automatically.
 *
 * <p>A new instance of this class is created for each function invocation, so
 * storing the enrolled object as a field is safe and shared between
 * {@link Govern @Govern}, {@link Enforce @Enforce}, and {@link Disregard @Disregard}.
 */
public class AuditGovernance {

	/** Retained between @Govern and @Enforce / @Disregard on the same invocation. */
	private Auditable enrolled;

	/**
	 * Called by OfficeFloor before the function body executes, once per enrolled
	 * managed object.  Analogous to beginning a transaction.
	 */
	@Govern
	public void govern(Auditable auditable) {
		this.enrolled = auditable;
		auditable.recordEvent("governance-begin");
	}

	/**
	 * Called by OfficeFloor after the function body completes successfully.
	 * Analogous to committing a transaction.
	 */
	@Enforce
	public void enforce() {
		enrolled.recordEvent("governance-commit");
	}

	/**
	 * Called by OfficeFloor when the function throws an unhandled exception.
	 * Analogous to rolling back a transaction.
	 */
	@Disregard
	public void disregard() {
		enrolled.recordEvent("governance-rollback");
	}
}
// END SNIPPET: tutorial
