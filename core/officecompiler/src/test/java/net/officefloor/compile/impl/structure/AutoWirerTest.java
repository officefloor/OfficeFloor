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
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests the {@link AutoWirer}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class AutoWirerTest {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context = new SourceContextImpl(this.getClass().getName(), false, null,
			AutoWirerTest.class.getClassLoader(), new MockClockFactory());

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * Mock {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this.mocks);

	/**
	 * {@link AutoWirer} to test.
	 */
	private AutoWirer<Node> wirer = new AutoWirerImpl<>(this.context, this.issues,
			AutoWireDirection.SOURCE_REQUIRES_TARGET);

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode office = this.mocks.createMock(OfficeNode.class);

	/**
	 * Source {@link LinkObjectNode}.
	 */
	private final LinkObjectNode source = this.mocks.createMock(LinkObjectNode.class);

	/**
	 * Target {@link LinkObjectNode}.
	 */
	private final LinkObjectNode target = this.mocks.createMock(LinkObjectNode.class);

	/**
	 * Ensure issue if no {@link AutoWire} provided.
	 */
	@Test
	public void issueIfNoAutoWire() {
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Must specify at least one source AutoWire");
		this.mocks.replayMockObjects();
		assertNull(this.wirer.getAutoWireLink(this.source), "Should be no link");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure issue if get no {@link AutoWire} instances.
	 */
	@Test
	public void issueIfGetNoAutoWires() {
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "No target found by auto-wiring from " + Object.class.getName());
		this.mocks.replayMockObjects();
		assertEquals(0, this.wirer.getAutoWireLinks(this.source, new AutoWire(Object.class)).length, "Should be no link");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure no issue if find {@link AutoWire} list is empty.
	 */
	@Test
	public void allowFindNoAutoWires() {
		this.mocks.replayMockObjects();
		assertEquals(0, this.wirer.findAutoWireLinks(this.source, new AutoWire(Object.class)).length, "Should be no link");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure only creates the one instance.
	 */
	@Test
	public void nodeFactory() {
		@SuppressWarnings("unchecked")
		Function<OfficeNode, LinkObjectNode> factory = this.mocks.createMock(Function.class);
		this.mocks.recordReturn(factory, factory.apply(this.office), this.target);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(factory, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object.class));
		this.assertLinkMatch(link, new AutoWire(Object.class), new AutoWire(Object.class));
		for (int i = 0; i < 10; i++) {
			assertSame(this.target, link.getTargetNode(this.office), "Should be same node");
			assertSame(this.target, link.getTargetNode(this.office), "Should only create once");
		}
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can match on type.
	 */
	@Test
	public void matchByType() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object.class));
		this.assertLinkMatch(link, new AutoWire(Object.class), new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can match on array.
	 */
	@Test
	public void matchByArray() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object[].class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object[].class));
		this.assertLinkMatch(link, new AutoWire(Object[].class), new AutoWire(Object[].class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can match on array name
	 */
	@Test
	public void matchByArrayName() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object[].class.getName()));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object[].class.getName()));
		this.assertLinkMatch(link, new AutoWire(Object[].class.getName()), new AutoWire(Object[].class.getName()));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can match on qualified type.
	 */
	@Test
	public void matchByQualifiedType() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(link, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can fall back to type for match.
	 */
	@Test
	public void matchFallBackToType() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(link, new AutoWire("QUALIFIED", Object.class), new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure match qualified before fall back to unqualified type.
	 */
	@Test
	public void matchQualifiedOverUnqualifiedType() {
		LinkObjectNode unqualified = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		this.wirer.addAutoWireTarget(unqualified, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire("QUALIFIED", Object.class));
		this.assertLinkMatch(link, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure if not qualifying, then selects unqualified.
	 */
	@Test
	public void matchUnqualifiedOverQualified() {
		LinkObjectNode qualified = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(qualified, new AutoWire("QUALIFIED", Object.class));
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object.class));
		this.assertLinkMatch(link, new AutoWire(Object.class), new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can match on child type.
	 */
	@Test
	public void matchChildType() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(ChildType.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(ParentType.class));
		this.assertLinkMatch(link, new AutoWire(ParentType.class), new AutoWire(ChildType.class));
		this.mocks.verifyMockObjects();
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
	@Test
	public void matchReverseChildType() {
		this.wirer = new AutoWirerImpl<Node>(this.context, this.issues, AutoWireDirection.TARGET_CATEGORISES_SOURCE);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(ParentType.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(ChildType.class));
		this.assertLinkMatch(link, new AutoWire(ChildType.class), new AutoWire(ParentType.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching target.
	 */
	@Test
	public void issueAsNoMatchingTarget() {
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "No target found by auto-wiring from " + NotMatch.class.getName());
		this.mocks.replayMockObjects();
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(NotMatch.class));
		assertNull(link, "Should be no matching links");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching target.
	 */
	@Test
	public void noIssueWhenNotFoundTarget() {
		this.mocks.replayMockObjects();
		AutoWireLink<Node, Node>[] links = this.wirer.findAutoWireLinks(this.source, new AutoWire("NOT_MATCH"));
		assertEquals(0, links.length, "Should be no matching links");
		this.mocks.verifyMockObjects();
	}

	@Test
	public void issueIfDuplicateMatchingTarget() {
		LinkObjectNode one = this.mocks.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Duplicate auto-wire targets (java.lang.Object -> java.lang.Object, java.lang.Object).  Please qualify to avoid this issue.");
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire(Object.class));
		this.wirer.addAutoWireTarget(two, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure match multiple target.
	 */
	@Test
	public void matchMultipleTarget() {
		this.doDuplicateMatchingTarget((targetAutoWires) -> this.wirer.getAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Ensure find multiple target.
	 */
	@Test
	public void foundMultipleTarget() {
		this.doDuplicateMatchingTarget((targetAutoWires) -> this.wirer.findAutoWireLinks(this.source, targetAutoWires));
	}

	/**
	 * Undertakes the test if duplicate matching target.
	 */
	private void doDuplicateMatchingTarget(Function<AutoWire[], AutoWireLink<Node, Node>[]> retriever) {
		LinkObjectNode one = this.mocks.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire(Object.class));
		this.wirer.addAutoWireTarget(two, new AutoWire(Object.class));
		AutoWireLink<Node, Node>[] links = retriever.apply(new AutoWire[] { new AutoWire(Object.class) });
		assertEquals(2, links.length, "Should match multiple links");
		for (int i = 0; i < 2; i++) {
			assertNull(links[i].getSourceAutoWire().getQualifier(), "Should be no source qualifier");
			assertEquals(Object.class.getName(), links[i].getSourceAutoWire().getType(), "Incorrect source type");
			assertSame(this.source, links[i].getSourceNode(), "Incorrect source node");
		}
		assertNull(links[0].getTargetAutoWire().getQualifier(), "Should be no first target qualifier");
		assertEquals(Object.class.getName(), links[0].getTargetAutoWire().getType(), "Incorrect first target type");
		assertSame(one, links[0].getTargetNode(this.office), "Incorrect first target node");
		assertNull(links[1].getTargetAutoWire().getQualifier(), "Should be no second target qualifier");
		assertEquals(Object.class.getName(), links[1].getTargetAutoWire().getType(), "Incorrect second target type");
		assertSame(two, links[1].getTargetNode(this.office), "Incorrect second target node");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can link when providing multiple {@link AutoWire} options.
	 */
	@Test
	public void multipleOptions() {
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source, new AutoWire(NotMatch.class),
				new AutoWire(Object.class));
		this.assertLinkMatch(link, new AutoWire(Object.class), new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Undertakes the test if multiple {@link AutoWire} matches to multiple targets.
	 */
	@Test
	public void issueIfMultipleMatchMultipleTargets() {
		LinkObjectNode one = this.mocks.createMock(LinkObjectNode.class);
		LinkObjectNode two = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(),
				"Multiple auto-wires (java.lang.Integer, java.lang.String) matching multiple targets (java.lang.Integer, java.lang.String).  Please qualify, reduce dependencies or remove auto-wire targets to avoid this issue.");
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(one, new AutoWire(Integer.class));
		this.wirer.addAutoWireTarget(two, new AutoWire(String.class));
		AutoWireLink<Node, Node> link = this.wirer.getAutoWireLink(this.source,
				new AutoWire(NotMatch.class), new AutoWire(Integer.class), new AutoWire(String.class));
		assertNull(link, "As match multiple, should not provide value");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in scope.
	 */
	@Test
	public void getInScope() {
		LinkObjectNode node = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		AutoWire autoWire = new AutoWire(Object.class);
		this.wirer.addAutoWireTarget(node, autoWire);
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(this.target, autoWire);
		AutoWireLink<Node, Node> link = scopeWirer.getAutoWireLink(this.source, autoWire);
		assertLinkMatch(link, autoWire, autoWire);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to find target in outer scope.
	 */
	@Test
	public void getInOuterScope() {
		LinkObjectNode node = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire(NotMatch.class));
		AutoWireLink<Node, Node> link = scopeWirer.getAutoWireLink(this.source, new AutoWire(Object.class));
		assertLinkMatch(link, new AutoWire(Object.class), new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to find better match in outer scope.
	 */
	@Test
	public void betterMatchInOuterScope() {
		LinkObjectNode node = this.mocks.createMock(LinkObjectNode.class);
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("QUALIFIED", Object.class));
		AutoWirer<Node> scopeWirer = this.wirer.createScopeAutoWirer();
		scopeWirer.addAutoWireTarget(node, new AutoWire(Object.class));
		AutoWireLink<Node, Node> link = scopeWirer.getAutoWireLink(this.source, new AutoWire("QUALIFIED", Object.class));
		assertLinkMatch(link, new AutoWire("QUALIFIED", Object.class), new AutoWire("QUALIFIED", Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown source type.
	 */
	@Test
	public void unknownSourceType() {
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Unable to load source auto-wire type UNKNOWN");
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire(Object.class));
		this.wirer.findAutoWireLinks(this.source, new AutoWire("UNKNOWN"));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown target type.
	 */
	@Test
	public void unknownTargetType() {
		this.mocks.recordReturn(this.source, this.source.getQualifiedName(), "SOURCE");
		this.issues.recordIssue("SOURCE", this.source.getClass(), "Unable to load target auto-wire type UNKNOWN");
		this.mocks.replayMockObjects();
		this.wirer.addAutoWireTarget(this.target, new AutoWire("UNKNOWN"));
		this.wirer.findAutoWireLinks(this.source, new AutoWire(Object.class));
		this.mocks.verifyMockObjects();
	}

	/**
	 * Assets matching {@link AutoWireLink}.
	 * 
	 * @param link          {@link AutoWireLink} instance.
	 * @param sourceAutoWire Expected source {@link AutoWire}.
	 * @param targetAutoWire Expected target {@link AutoWire}.
	 * @return Matching {@link AutoWireLink}.
	 */
	private void assertLinkMatch(AutoWireLink<Node, Node> link, AutoWire sourceAutoWire,
			AutoWire targetAutoWire) {
		assertNotNull(link, "Should have link");
		assertEquals(sourceAutoWire.getQualifier(), link.getSourceAutoWire().getQualifier(), "Incorrect source qualifier");
		assertEquals(sourceAutoWire.getType(), link.getSourceAutoWire().getType(), "Incorrect source type");
		assertSame(this.source, link.getSourceNode(), "Incorrect source node");
		assertEquals(targetAutoWire.getQualifier(), link.getTargetAutoWire().getQualifier(), "Incorrect target qualifier");
		assertEquals(targetAutoWire.getType(), link.getTargetAutoWire().getType(), "Incorrect target type");
		assertSame(this.target, link.getTargetNode(this.office), "Incorrect target node");
	}

	public static class NotMatch {
	}

}
