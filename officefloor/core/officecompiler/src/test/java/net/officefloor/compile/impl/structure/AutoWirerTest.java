/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.structure;

import java.util.function.Function;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireDirection;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.test.MockClockFactory;
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
	private final SourceContext context = new SourceContextImpl(this.getClass().getName(), false, null,
			AutoWirerTest.class.getClassLoader(), new MockClockFactory());

	/**
	 * Mock {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link AutoWirer} to test.
	 */
	private AutoWirer<Node> wirer = new AutoWirerImpl<>(this.context, this.issues,
			AutoWireDirection.SOURCE_REQUIRES_TARGET);

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode office = this.createMock(OfficeNode.class);

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
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
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
		Function<OfficeNode, LinkObjectNode> factory = this.createMock(Function.class);
		this.recordReturn(factory, factory.apply(this.office), this.target);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(factory, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(Object.class));
		for (int i = 0; i < 10; i++) {
			AutoWireLink<Node, Node> link = this.assertLinkMatch(links, new AutoWire(Object.class),
					new AutoWire(Object.class));
			assertSame("Should be same node", this.target, link.getTargetNode(this.office));
			assertSame("Should only create once", this.target, link.getTargetNode(this.office));
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on type.
	 */
	public void testMatchByType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(Object.class));
		this.assertLinkMatch(links, new AutoWire(Object.class), new AutoWire(Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on array.
	 */
	public void testMatchByArray() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object[].class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(Object[].class));
		this.assertLinkMatch(links, new AutoWire(Object[].class), new AutoWire(Object[].class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on array name
	 */
	public void testMatchByArrayName() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object[].class.getName()));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source,
				new AutoWire(Object[].class.getName()));
		this.assertLinkMatch(links, new AutoWire(Object[].class.getName()), new AutoWire(Object[].class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on qualified type.
	 */
	public void testMatchByQualifiedType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source,
				new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can fall back to type for match.
	 */
	public void testMatchFallBackToType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source,
				new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", Object.class), new AutoWire(Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure match qualified before fall back to unqualified type.
	 */
	public void testMatchQualifiedOverUnqualifiedType() {
		LinkObjectNode unqualified = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		this.wirer.addAutoWireTarget(unqualified, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source,
				new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(links, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure if not qualifying, then selects unqualified.
	 */
	public void testMatchUnqualifiedOverQualified() {
		LinkObjectNode qualified = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(qualified, new AutoWire("QUALIFIED", Object.class));
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(Object.class));
		this.assertLinkMatch(links, new AutoWire(Object.class), new AutoWire(Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can match on child type.
	 */
	public void testMatchChildType() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(ChildType.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(ParentType.class));
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
	 * Ensure can match on reverse of child type.
	 */
	public void testMatchReverseChildType() {
		this.wirer = new AutoWirerImpl<Node>(this.context, this.issues, AutoWireDirection.TARGET_CATEGORISES_SOURCE);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(ParentType.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(ChildType.class));
		this.assertLinkMatch(links, new AutoWire(ChildType.class), new AutoWire(ParentType.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching target.
	 */
	public void testIssueAsNoMatchingTarget() {
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "No target found by auto-wiring");
		this.replayMockObjects();
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire("NOT_MATCH"));
		assertEquals("Should be no matching links", 0, links.length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching target.
	 */
	public void testNoIssueWhenNotFoundTarget() {
		this.replayMockObjects();
		AutoWireLink<Node, Node>[] links = this.wirer.findAutoWireLinks(this.source, new AutoWire("NOT_MATCH"));
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
	private void doIssueIfDuplicateMatchingTarget(Function<AutoWire[], AutoWireLink<Node, Node>[]> retriever) {
		LinkObjectNode one = this.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.createMock(LinkObjectNode.class);
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Duplicate auto-wire targets (java.lang.Object -> java.lang.Object, java.lang.Object).  Please qualify to avoid this issue.");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire(Object.class));
		this.wirer.addAutoWireTarget(two, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = retriever.apply(new AutoWire[] { new AutoWire(Object.class) });
		assertEquals("Should match multiple links", 2, links.length);
		for (int i = 0; i < 2; i++) {
			assertNull("Should be no source qualifier", links[i].getSourceAutoWire().getQualifier());
			assertEquals("Incorrect source type", Object.class.getName(), links[i].getSourceAutoWire().getType());
			assertSame("Incorrect source node", this.source, links[i].getSourceNode());
		}
		assertNull("Should be no first target qualifier", links[0].getTargetAutoWire().getQualifier());
		assertEquals("Incorrect first target type", Object.class.getName(), links[0].getTargetAutoWire().getType());
		assertSame("Incorrect first target node", one, links[0].getTargetNode(this.office));
		assertNull("Should be no second target qualifier", links[1].getTargetAutoWire().getQualifier());
		assertEquals("Incorrect second target type", Object.class.getName(), links[1].getTargetAutoWire().getType());
		assertSame("Incorrect second target node", two, links[1].getTargetNode(this.office));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link when providing multiple {@link AutoWire} options.
	 */
	public void testMultipleOptions() {
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = this.wirer.getAutoWireLinks(this.source, new AutoWire(NotMatch.class),
				new AutoWire(Object.class));
		this.assertLinkMatch(links, new AutoWire(Object.class), new AutoWire(Object.class));
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
	 * Undertakes the test if multiple {@link AutoWire} matches to multiple targets.
	 */
	public void doIssueIfMultipleMatchMultipleTargets(Function<AutoWire[], AutoWireLink<Node, Node>[]> retriever) {
		LinkObjectNode one = this.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.createMock(LinkObjectNode.class);
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Multiple auto-wires (java.lang.Integer, java.lang.String) matching multiple targets (java.lang.Integer, java.lang.String).  Please qualify, reduce dependencies or remove auto-wire targets to avoid this issue.");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire(Integer.class));
		this.wirer.addAutoWireTarget(two, new AutoWire(String.class));
		AutoWireLink<Node, Node>[] links = retriever.apply(new AutoWire[] { new AutoWire(NotMatch.class),
				new AutoWire(Integer.class), new AutoWire(String.class) });
		assertEquals("Incorrect number of links", 2, links.length);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in scope.
	 */
	public void testGetInScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		AutoWire autoWire = new AutoWire(Object.class);
		this.wirer.addAutoWireTarget(node, autoWire);
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(this.target, autoWire);
		AutoWireLink<Node, Node>[] links = scopeWirer.getAutoWireLinks(this.source, autoWire);
		assertLinkMatch(links, autoWire, autoWire);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in outer scope.
	 */
	public void testGetInOuterScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire(NotMatch.class));
		AutoWireLink<Node, Node>[] links = scopeWirer.getAutoWireLinks(this.source, new AutoWire(Object.class));
		assertLinkMatch(links, new AutoWire(Object.class), new AutoWire(Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to find better match in outer scope.
	 */
	public void testBetterMatchInOuterScope() {
		LinkObjectNode node = this.createMock(LinkObjectNode.class);
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = scopeWirer.getAutoWireLinks(this.source,
				new AutoWire("QUALIFIED", Object.class));
		assertLinkMatch(links, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown source type.
	 */
	public void testUnknownSourceType() {
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Unable to load source auto-wire type UNKNOWN");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		this.wirer.findAutoWireLinks(this.source, new AutoWire("UNKNOWN"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown target type.
	 */
	public void testUnknownTargetType() {
		this.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Unable to load target auto-wire type UNKNOWN");
		this.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("UNKNOWN"));
		this.wirer.findAutoWireLinks(this.source, new AutoWire(Object.class));
		this.verifyMockObjects();
	}

	/**
	 * Assets matching {@link AutoWireLink}.
	 * 
	 * @param links          {@link AutoWireLink} instances.
	 * @param sourceAutoWire Expected source {@link AutoWire}.
	 * @param targetAutoWire Expected target {@link AutoWire}.
	 * @return Matching {@link AutoWireLink}.
	 */
	private AutoWireLink<Node, Node> assertLinkMatch(AutoWireLink<Node, Node>[] links, AutoWire sourceAutoWire,
			AutoWire targetAutoWire) {
		assertEquals("Should only be one matching link", 1, links.length);
		AutoWireLink<Node, Node> link = links[0];
		assertEquals("Incorrect source qualifier", sourceAutoWire.getQualifier(),
				link.getSourceAutoWire().getQualifier());
		assertEquals("Incorrect source type", sourceAutoWire.getType(), link.getSourceAutoWire().getType());
		assertSame("Incorrect source node", this.source, link.getSourceNode());
		assertEquals("Incorrect target qualifier", targetAutoWire.getQualifier(),
				link.getTargetAutoWire().getQualifier());
		assertEquals("Incorrect target type", targetAutoWire.getType(), link.getTargetAutoWire().getType());
		assertSame("Incorrect target node", this.target, link.getTargetNode(this.office));
		return link;
	}

	public static class NotMatch {
	}

}
