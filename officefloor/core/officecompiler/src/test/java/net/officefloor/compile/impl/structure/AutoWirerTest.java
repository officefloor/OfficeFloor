/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.autowire.impl.AutoWireTest;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AutoWirer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWirerTest extends OfficeFrameTestCase {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context = new SourceContextImpl(false, AutoWireTest.class.getClassLoader());

	/**
	 * Mock {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link AutoWirer} to test.
	 */
	private final AutoWirer<Node> wirer = new AutoWirerImpl<>(this.context, this.issues);

	/**
	 * Source {@link LinkObjectNode}.
	 */
	private final LinkObjectNode source = this.createMock(LinkObjectNode.class);

	/**
	 * Target {@link LinkObjectNode}.
	 */
	private final LinkObjectNode target = this.createMock(LinkObjectNode.class);

	/**
	 * Ensure issue if no {@link AutoWire} provided.
	 */
	public void testIssueIfNoAutoWire() {
		this.recordReturn(this.source, this.source.getNodeName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Must specify at least one AutoWire");
		this.replayMockObjects();
		assertEquals("Should be no link", 0, this.wirer.getAutoWireLinks(this.source).length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure only creates the one instance.
	 */
	public void testNodeFactory() {
		@SuppressWarnings("unchecked")
		Supplier<LinkObjectNode> supplier = this.createMock(Supplier.class);
		this.recordReturn(supplier, supplier.get(), this.target);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(supplier, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("TYPE"));
		for (int i = 0; i < 10; i++) {
			AutoWireLink<Node> link = this.assertLinkMatch(links, new AutoWire("TYPE"), new AutoWire("TYPE"));
			assertSame("Should be same node", this.target, link.getTargetNode());
			assertSame("Should only create once", this.target, link.getTargetNode());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on type.
	 */
	public void testMatchByType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("TYPE"));
		this.assertLinkMatch(links, new AutoWire("TYPE"), new AutoWire("TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on qualified type.
	 */
	public void testMatchByQualifiedType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", "TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("QUALIFIED", "TYPE"));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", "TYPE"), new AutoWire("QUALIFIED", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can fall back to type for match.
	 */
	public void testMatchFallBackToType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("QUALIFIED", "TYPE"));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", "TYPE"), new AutoWire("TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure match qualified before fall back to unqualified type.
	 */
	public void testMatchQualifiedOverUnqualifiedType() {
		LinkObjectNode unqualified = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", "TYPE"));
		this.wirer.addAutoWireTarget(unqualified, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("QUALIFIED", "TYPE"));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", "TYPE"), new AutoWire("QUALIFIED", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure if not qualifying, then selects unqualified.
	 */
	public void testMatchUnqualifiedOverQualified() {
		LinkObjectNode qualified = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(qualified, new AutoWire("QUALIFIED", "TYPE"));
		this.wirer.addAutoWireTarget(this.target, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("TYPE"));
		this.assertLinkMatch(links, new AutoWire("TYPE"), new AutoWire("TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on child type.
	 */
	public void testMatchChildType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(ChildType.class));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(ParentType.class));
		this.assertLinkMatch(links, new AutoWire(ParentType.class), new AutoWire(ChildType.class));
		this.verifyMockObjects();
	}

	/**
	 * Parent type.
	 */
	private static interface ParentType {
	}

	/**
	 * Child type.
	 */
	private static interface ChildType extends ParentType {
	}

	/**
	 * Ensure issue if no matching target.
	 */
	public void testIssueAsNoMatchingTarget() {
		this.recordReturn(this.source, this.source.getNodeName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "No target found by auto-wiring");
		this.replayMockObjects();
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("NOT_MATCH"));
		assertEquals("Should be no matching links", 0, links.length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching target.
	 */
	public void testNoIssueWhenNotFoundTarget() {
		this.replayMockObjects();
		AutoWireLink<Node>[] links = this.wirer.findAutoWireLinks(this.source, new AutoWire("NOT_MATCH"));
		assertEquals("Should be no matching links", 0, links.length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if duplicate matching target.
	 */
	public void testIssueIfDuplicateMatchingTarget() {
		this.doIssueIfDuplicateMatchingTarget(
				(targetAutoWires) -> this.wirer.getAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Ensure issue if duplicate found target.
	 */
	public void testIssueIfDuplicateFoundTarget() {
		this.doIssueIfDuplicateMatchingTarget(
				(targetAutoWires) -> this.wirer.findAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Undertakes the test if duplicate matching target.
	 */
	private void doIssueIfDuplicateMatchingTarget(Function<AutoWire[], AutoWireLink<Node>[]> retriever) {
		LinkObjectNode one = this.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.createMock(LinkObjectNode.class);
		this.recordReturn(this.source, this.source.getNodeName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Duplicate auto-wire targets (TYPE -> TYPE, TYPE).  Please qualify to avoid this issue.");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire("TYPE"));
		this.wirer.addAutoWireTarget(two, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = retriever.apply(new AutoWire[] { new AutoWire("TYPE") });
		assertEquals("Should match multiple links", 2, links.length);
		for (int i = 0; i < 2; i++) {
			assertNull("Should be no source qualifier", links[i].getSourceAutoWire().getQualifier());
			assertEquals("Incorrect source type", "TYPE", links[i].getSourceAutoWire().getType());
			assertSame("Incorrect source node", this.source, links[i].getSourceNode());
		}
		assertNull("Should be no first target qualifier", links[0].getTargetAutoWire().getQualifier());
		assertEquals("Incorrect first target type", "TYPE", links[0].getTargetAutoWire().getType());
		assertSame("Incorrect first target node", one, links[0].getTargetNode());
		assertNull("Should be no second target qualifier", links[1].getTargetAutoWire().getQualifier());
		assertEquals("Incorrect second target type", "TYPE", links[1].getTargetAutoWire().getType());
		assertSame("Incorrect second target node", two, links[1].getTargetNode());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link when providing multiple {@link AutoWire} options.
	 */
	public void testMultipleOptions() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("NOT_MATCH"),
				new AutoWire("TYPE"));
		this.assertLinkMatch(links, new AutoWire("TYPE"), new AutoWire("TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if multiple {@link AutoWire} matches to multiple targets.
	 */
	public void testIssueIfMultipleMatchMultipleTargets() {
		this.doIssueIfMultipleMatchMultipleTargets(
				(targetAutoWires) -> this.wirer.getAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Ensure issue if multiple {@link AutoWire} found to multiple targets.
	 */
	public void testIssueIfMultipleFoundMultipleTargets() {
		this.doIssueIfMultipleMatchMultipleTargets(
				(targetAutoWires) -> this.wirer.findAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Undertakes the test if multiple {@link AutoWire} matches to multiple
	 * targets.
	 */
	public void doIssueIfMultipleMatchMultipleTargets(Function<AutoWire[], AutoWireLink<Node>[]> retriever) {
		LinkObjectNode one = this.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.createMock(LinkObjectNode.class);
		this.recordReturn(this.source, this.source.getNodeName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Multiple auto-wires (ONE, TWO) matching multiple targets (ONE, TWO).  Please qualify, reduce dependencies or remove auto-wire targets to avoid this issue.");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire("ONE"));
		this.wirer.addAutoWireTarget(two, new AutoWire("TWO"));
		AutoWireLink<Node>[] links = retriever
				.apply(new AutoWire[] { new AutoWire("UNMATCHED"), new AutoWire("ONE"), new AutoWire("TWO") });
		assertEquals("Incorrect number of links", 2, links.length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in scope.
	 */
	public void testGetInScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		AutoWire autoWire = new AutoWire("TYPE");
		this.wirer.addAutoWireTarget(node, autoWire);
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(this.target, autoWire);
		AutoWireLink<Node>[] links = scopeWirer.getAutoWireLinks(this.source, autoWire);
		assertLinkMatch(links, autoWire, autoWire);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in outer scope.
	 */
	public void testGetInOuterScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("TYPE"));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire("NOT_MATCH"));
		AutoWireLink<Node>[] links = scopeWirer.getAutoWireLinks(this.source, new AutoWire("TYPE"));
		assertLinkMatch(links, new AutoWire("TYPE"), new AutoWire("TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find better match in outer scope.
	 */
	public void testBetterMatchInOuterScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", "TYPE"));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire("TYPE"));
		AutoWireLink<Node>[] links = scopeWirer.getAutoWireLinks(this.source, new AutoWire("QUALIFIED", "TYPE"));
		assertLinkMatch(links, new AutoWire("QUALIFIED", "TYPE"), new AutoWire("QUALIFIED", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Assets matching {@link AutoWireLink}.
	 * 
	 * @param links
	 *            {@link AutoWireLink} instances.
	 * @param sourceAutoWire
	 *            Expected source {@link AutoWire}.
	 * @param targetAutoWire
	 *            Expected target {@link AutoWire}.
	 * @return Matching {@link AutoWireLink}.
	 */
	private AutoWireLink<Node> assertLinkMatch(AutoWireLink<Node>[] links, AutoWire sourceAutoWire,
			AutoWire targetAutoWire) {
		assertEquals("Should only be one matching link", 1, links.length);
		AutoWireLink<Node> link = links[0];
		assertEquals("Incorrect source qualifier", sourceAutoWire.getQualifier(),
				link.getSourceAutoWire().getQualifier());
		assertEquals("Incorrect source type", sourceAutoWire.getType(), link.getSourceAutoWire().getType());
		assertSame("Incorrect source node", this.source, link.getSourceNode());
		assertEquals("Incorrect target qualifier", targetAutoWire.getQualifier(),
				link.getTargetAutoWire().getQualifier());
		assertEquals("Incorrect target type", targetAutoWire.getType(), link.getTargetAutoWire().getType());
		assertSame("Incorrect target node", this.target, link.getTargetNode());
		return link;
	}

}