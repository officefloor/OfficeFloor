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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link AutoWirer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWirerImpl<N extends Node> implements AutoWirer<N> {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Target {@link AutoWireLink} instances.
	 */
	private final List<AutoWireNodeImpl> targets = new ArrayList<>();

	/**
	 * Outer scope {@link AutoWirer}.
	 */
	private final AutoWirerImpl<N> outerScope;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public AutoWirerImpl(SourceContext context, CompilerIssues issues) {
		this(context, issues, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param outerScope
	 *            Outer scope {@link AutoWirer}.
	 */
	private AutoWirerImpl(SourceContext context, CompilerIssues issues, AutoWirerImpl<N> outerScope) {
		this.context = context;
		this.issues = issues;
		this.outerScope = outerScope;
	}

	/*
	 * ===================== AutoWirer ============================
	 */

	@Override
	public void addAutoWireTarget(N targetNode, AutoWire... targetAutoWires) {
		this.targets.add(new AutoWireNodeImpl(targetAutoWires, targetNode));
	}

	@Override
	public void addAutoWireTarget(Function<OfficeNode, ? extends N> targetNodeFactory, AutoWire... targetAutoWires) {
		this.targets.add(new AutoWireNodeImpl(targetAutoWires, targetNodeFactory));
	}

	@Override
	public AutoWireLink<N>[] findAutoWireLinks(N sourceNode, AutoWire... sourceAutoWires) {
		return this.sourceAutoWireLinks(false, sourceNode, sourceAutoWires);
	}

	@Override
	public AutoWireLink<N>[] getAutoWireLinks(final N sourceNode, AutoWire... sourceAutoWires) {
		return this.sourceAutoWireLinks(true, sourceNode, sourceAutoWires);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private AutoWireLink<N>[] sourceAutoWireLinks(boolean isMustMatch, final N sourceNode,
			AutoWire... sourceAutoWires) {

		// Ensure have at least one auto-wire
		if ((sourceAutoWires == null) || (sourceAutoWires.length == 0)) {
			this.issues.addIssue(sourceNode, "Must specify at least one AutoWire");
			return new AutoWireLink[0];
		}

		// Undertake the matching following order
		for (int i = 0; i < 6; i++) {

			// Search through the scopes
			AutoWirerImpl<N> currentScope = this;
			while (currentScope != null) {

				// Match by qualifier (if available) and type
				final int index = i;
				AutoWireLink[] matches = currentScope.targets.stream().map((target) -> {

					// Determine if match
					NEXT_AUTO_WIRE: for (AutoWire sourceAutoWire : sourceAutoWires) {

						// Obtain the auto-wire details
						String sourceType = sourceAutoWire.getType();
						String sourceQualifier = sourceAutoWire.getQualifier();

						// Determine if match target auto-wire
						for (AutoWire targetAutoWire : target.autoWires) {

							// Must always match on type
							if ((index % 2) == 0) {
								// Match on exact type
								if (!(sourceType.equals(targetAutoWire.getType()))) {
									continue NEXT_AUTO_WIRE;
								}

							} else {
								// Match on child type
								Class<?> sourceClass = this.context.loadOptionalClass(sourceType);
								Class<?> targetClass = this.context.loadOptionalClass(targetAutoWire.getType());
								if ((sourceClass == null) || (targetClass == null)) {
									continue NEXT_AUTO_WIRE; // must load to
																// match
								} else if (!sourceClass.isAssignableFrom(targetClass)) {
									continue NEXT_AUTO_WIRE;
								}
							}

							// Determine qualifier matching
							switch (index) {
							case 0:
							case 1:
								// Match with qualifier
								if ((sourceQualifier != null)
										&& (sourceQualifier.equals(targetAutoWire.getQualifier()))) {
									return new AutoWireLinkImpl(sourceNode, sourceAutoWire, target, targetAutoWire);
								}
								break;

							case 2:
							case 3:
								// Match only by type
								if ((sourceQualifier == null) && (targetAutoWire.getQualifier() == null)) {
									return new AutoWireLinkImpl(sourceNode, sourceAutoWire, target, targetAutoWire);
								}
								break;

							case 4:
							case 5:
								// Match falling back to type
								if (targetAutoWire.getQualifier() == null) {
									return new AutoWireLinkImpl(sourceNode, sourceAutoWire, target, targetAutoWire);
								}
								break;
							}
						}
					}

					// As here, do not include
					return null;

				}).filter((link) -> link != null).toArray(AutoWireLink[]::new);
				switch (matches.length) {
				case 0:
					// No match, so attempt next try
					break;
				case 1:
					// Found match
					return matches;
				default:
					// Multiple matches, so create listing of auto-wires
					AutoWire[] matchSourceAutoWires = Arrays.stream(matches).map((link) -> link.getSourceAutoWire())
							.distinct().sorted().toArray(AutoWire[]::new);
					AutoWire[] matchTargetAutoWires = Arrays.stream(matches).map((link) -> link.getTargetAutoWire())
							.sorted().toArray(AutoWire[]::new);

					// Indicate issue regarding the matches
					StringBuilder sourceMatches = new StringBuilder();
					Arrays.stream(matchSourceAutoWires).forEach((autoWire) -> {
						if (sourceMatches.length() != 0) {
							sourceMatches.append(", ");
						}
						sourceMatches.append(autoWire.toString());
					});
					StringBuilder targetMatches = new StringBuilder();
					Arrays.stream(matchTargetAutoWires).forEach((autoWire) -> {
						if (targetMatches.length() != 0) {
							targetMatches.append(", ");
						}
						targetMatches.append(autoWire.toString());
					});
					if (matchSourceAutoWires.length == 1) {
						this.issues.addIssue(sourceNode, "Duplicate auto-wire targets (" + sourceMatches.toString()
								+ " -> " + targetMatches.toString() + ").  Please qualify to avoid this issue.");
					} else {
						this.issues.addIssue(sourceNode, "Multiple auto-wires (" + sourceMatches.toString()
								+ ") matching multiple targets (" + targetMatches.toString()
								+ ").  Please qualify, reduce dependencies or remove auto-wire targets to avoid this issue.");
					}

					// Return the matches
					return matches;
				}

				// Try outer scope
				currentScope = currentScope.outerScope;
			}
		}

		// As here no match
		if (isMustMatch) {
			this.issues.addIssue(sourceNode, "No target found by auto-wiring");
		}
		return new AutoWireLink[0];
	}

	@Override
	public AutoWirer<N> createScopeAutoWirer() {
		return new AutoWirerImpl<>(this.context, this.issues, this);
	}

	/**
	 * {@link AutoWireLink} implementation.
	 */
	private class AutoWireNodeImpl {

		/**
		 * {@link AutoWire} instances for this {@link Node}.
		 */
		private final AutoWire[] autoWires;

		/**
		 * Factory to create the {@link Node}.
		 */
		private final Function<OfficeNode, ? extends N> nodeFactory;

		/**
		 * {@link Node}.
		 */
		private N node = null;

		/**
		 * Instantiate.
		 * 
		 * @param autoWires
		 *            {@link AutoWire} instances for this {@link Node}.
		 * @param node
		 *            {@link Node}.
		 */
		private AutoWireNodeImpl(AutoWire[] autoWires, N node) {
			this.autoWires = autoWires;
			this.nodeFactory = (office) -> node;
			this.node = node;
		}

		/**
		 * Instantiate.
		 * 
		 * @param autoWires
		 *            {@link AutoWire} instances for this {@link Node}.
		 * @param nodeFactory
		 *            Factory to create the {@link Node}.
		 */
		private AutoWireNodeImpl(AutoWire[] autoWires, Function<OfficeNode, ? extends N> nodeFactory) {
			this.autoWires = autoWires;
			this.nodeFactory = nodeFactory;
		}

		/**
		 * Obtains the {@link Node}.
		 * 
		 * @param office
		 *            {@link OfficeNode}.
		 * @return {@link Node}.
		 */
		public N getNode(OfficeNode office) {
			if (this.node == null) {
				this.node = this.nodeFactory.apply(office);
			}
			return this.node;
		}
	}

	/**
	 * {@link AutoWireLink} implementation.
	 */
	private class AutoWireLinkImpl implements AutoWireLink<N> {

		/**
		 * Source {@link Node}.
		 */
		private final N sourceNode;

		/**
		 * Source {@link AutoWire}.
		 */
		private final AutoWire sourceAutoWire;

		/**
		 * Target {@link Node}.
		 */
		private final AutoWireNodeImpl targetNode;

		/**
		 * Target {@link AutoWire}.
		 */
		private final AutoWire targetAutoWire;

		/**
		 * Instantiate.
		 * 
		 * @param sourceNode
		 *            Source {@link Node}.
		 * @param sourceAutoWire
		 *            Source {@link AutoWire}.
		 * @param targetNode
		 *            Target {@link AutoWireNodeImpl} to obtain the target
		 *            {@link Node}.
		 * @param targetAutoWire
		 *            Target {@link AutoWire}.
		 */
		public AutoWireLinkImpl(N sourceNode, AutoWire sourceAutoWire, AutoWirerImpl<N>.AutoWireNodeImpl targetNode,
				AutoWire targetAutoWire) {
			this.sourceNode = sourceNode;
			this.sourceAutoWire = sourceAutoWire;
			this.targetNode = targetNode;
			this.targetAutoWire = targetAutoWire;
		}

		/*
		 * ================== AutoWireLink =========================
		 */

		@Override
		public N getSourceNode() {
			return this.sourceNode;
		}

		@Override
		public AutoWire getSourceAutoWire() {
			return this.sourceAutoWire;
		}

		@Override
		public N getTargetNode(OfficeNode office) {
			return this.targetNode.getNode(office);
		}

		@Override
		public AutoWire getTargetAutoWire() {
			return this.targetAutoWire;
		}
	}

}