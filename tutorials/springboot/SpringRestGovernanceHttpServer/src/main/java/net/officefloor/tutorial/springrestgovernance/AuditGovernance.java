package net.officefloor.tutorial.springrestgovernance;

import net.officefloor.plugin.governance.clazz.Disregard;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

// START SNIPPET: tutorial
public class AuditGovernance {

	private Auditable enrolled;

	@Govern
	public void govern(Auditable auditable) {
		this.enrolled = auditable;
		auditable.recordEvent("governance-begin");
	}

	@Enforce
	public void enforce() {
		enrolled.recordEvent("governance-commit");
	}

	@Disregard
	public void disregard() {
		enrolled.recordEvent("governance-rollback");
	}
}
// END SNIPPET: tutorial
