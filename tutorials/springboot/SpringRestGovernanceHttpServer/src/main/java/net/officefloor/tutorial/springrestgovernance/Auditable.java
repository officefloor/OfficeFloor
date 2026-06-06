package net.officefloor.tutorial.springrestgovernance;

/**
 * Governance extension interface.
 *
 * <p>Any managed object that implements this interface is automatically enrolled
 * in the {@code audit} governance when that governance is applied to the enclosing
 * function (via {@code govern: [ audit ]} in the REST YAML).  OfficeFloor detects
 * the assignability at compile time through
 * {@link net.officefloor.compile.spi.office.OfficeGovernance#enableAutoWireExtensions()}.
 */
// START SNIPPET: tutorial
public interface Auditable {

	void recordEvent(String event);
}
// END SNIPPET: tutorial
