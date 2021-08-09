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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireDirection;
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
	 * Direction of the {@link AutoWire}.
	 */
	private final AutoWireDirection direction;

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
	 * @param context   {@link SourceContext}.
	 * @param issues    {@link CompilerIssues}.
	 * @param direction {@link AutoWireDirection}.
	 */
	public AutoWirerImpl(SourceContext context, CompilerIssues issues, AutoWireDirection direction) {
		this(context, issues, direction, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param context    {@link SourceContext}.
	 * @param issues     {@link CompilerIssues}.
	 * @param direction  {@link AutoWireDirection}.
	 * @param outerScope Outer scope {@link AutoWirer}.
	 */
	private AutoWirerImpl(SourceContext context, CompilerIssues issues, AutoWireDirection direction,
			AutoWirerImpl<N> outerScope) {
		this.context = context;
		this.issues = issues;
		this.direction = direction;
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
	public <S extends Node> AutoWireLink<S, N>[] findAutoWireLinks(S sourceNode, AutoWire... sourceAutoWires) {
		return this.sourceAutoWireLinks(false, sourceNode, sourceAutoWires);
	}

	@Override
	public <S extends Node> AutoWireLink<S, N>[] getAutoWireLinks(final S sourceNode, AutoWire... sourceAutoWires) {
		return this.sourceAutoWireLinks(true, sourceNode, sourceAutoWires);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <S extends Node> AutoWireLink<S, N>[] sourceAutoWireLinks(boolean isMustMatch, final S sourceNode,
			AutoWire... sourceAutoWires) {

		// Ensure have at least one auto-wire
		if ((sourceAutoWires == null) || (sourceAutoWires.length == 0)) {
			this.issues.addIssue(sourceNode, "Must specify at least one AutoWire");
			return new AutoWireLink[0];
		}

		// Report unknown types only once
		Set<String> unknownSourceTypes = new HashSet<>();
		Set<String> unknownTargetTypes = new HashSet<>();

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
								// Obtain the source type
								Class<?> sourceClass = sourceAutoWire.getTypeClass(this.context);
								if (sourceClass == null) {
									// Only report issue once
									if (!unknownSourceTypes.contains(sourceType)) {
										unknownSourceTypes.add(sourceType);
										this.issues.addIssue(sourceNode,
												"Unable to load source auto-wire type " + sourceType);
									}
									continue NEXT_AUTO_WIRE;
								}

								// Obtain the target type
								Class<?> targetClass = targetAutoWire.getTypeClass(this.context);
								if (targetClass == null) {
									// Only report issue once
									String targetType = targetAutoWire.getType();
									if (!unknownTargetTypes.contains(targetType)) {
										unknownTargetTypes.add(targetType);
										this.issues.addIssue(sourceNode,
												"Unable to load target auto-wire type " + targetType);
									}
									continue NEXT_AUTO_WIRE;
								}

								// Match on child type (respecting direction)
								switch (this.direction) {
								case SOURCE_REQUIRES_TARGET:
									if (!sourceClass.isAssignableFrom(targetClass)) {
										continue NEXT_AUTO_WIRE;
									}
									break;

								case TARGET_CATEGORISES_SOURCE:
									if (!targetClass.isAssignableFrom(sourceClass)) {
										continue NEXT_AUTO_WIRE;
									}
									break;

								default:
									throw new IllegalStateException("Unknown auto-wire direction: " + this.direction);
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
		return new AutoWirerImpl<>(this.context, this.issues, this.direction, this);
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
		 * @param autoWires {@link AutoWire} instances for this {@link Node}.
		 * @param node      {@link Node}.
		 */
		private AutoWireNodeImpl(AutoWire[] autoWires, N node) {
			this.autoWires = autoWires;
			this.nodeFactory = (office) -> node;
			this.node = node;
		}

		/**
		 * Instantiate.
		 * 
		 * @param autoWires   {@link AutoWire} instances for this {@link Node}.
		 * @param nodeFactory Factory to create the {@link Node}.
		 */
		private AutoWireNodeImpl(AutoWire[] autoWires, Function<OfficeNode, ? extends N> nodeFactory) {
			this.autoWires = autoWires;
			this.nodeFactory = nodeFactory;
		}

		/**
		 * Obtains the {@link Node}.
		 * 
		 * @param office {@link OfficeNode}.
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
	private class AutoWireLinkImpl<S extends Node> implements AutoWireLink<S, N> {

		/**
		 * Source {@link Node}.
		 */
		private final S sourceNode;

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
		 * @param sourceNode     Source {@link Node}.
		 * @param sourceAutoWire Source {@link AutoWire}.
		 * @param targetNode     Target {@link AutoWireNodeImpl} to obtain the target
		 *                       {@link Node}.
		 * @param targetAutoWire Target {@link AutoWire}.
		 */
		public AutoWireLinkImpl(S sourceNode, AutoWire sourceAutoWire, AutoWirerImpl<N>.AutoWireNodeImpl targetNode,
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
		public S getSourceNode() {
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
