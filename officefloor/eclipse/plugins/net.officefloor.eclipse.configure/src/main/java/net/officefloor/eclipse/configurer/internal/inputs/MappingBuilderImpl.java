/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.internal.inputs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link MappingBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MappingBuilderImpl<M> extends AbstractBuilder<M, Map<String, String>, MappingBuilder<M>>
		implements MappingBuilder<M> {

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public MappingBuilderImpl(String label) {
		super(label);
	}

	/**
	 * Create {@link Node} input with mapping.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ValueInput createInput(Property<Map<String, String>> value) {

		// Create editor configuration
		AdaptedEditorModule module = new AdaptedEditorModule((builder) -> {
			AdaptedRootBuilder<MappingModel, Object> root = builder.root(MappingModel.class, (model) -> this);

			// Source
			AdaptedParentBuilder<MappingModel, Object, SourceModel, ?> source = root
					.parent(new SourceModel("Source", 0, 0), (mapping) -> mapping.getSources(), (mapping, ctx) -> {
						HBox pane = new HBox();
						ctx.label(pane);
						ctx.addNode(pane, ctx.connector(MappingConnection.class));
						return pane;
					});
			source.label((s) -> s.label);

			// Target
			root.parent(new TargetModel("Target", 0, 0), (mapping) -> Arrays.asList(new TargetModel("Target", 0, 0)),
					(mapping, ctx) -> {
						HBox pane = new HBox();
						ctx.addNode(pane, ctx.connector(MappingConnection.class));
						ctx.label(pane);
						return pane;
					}).label((t) -> t.label);

			// Connection
			source.connectOne(MappingConnection.class, (s) -> s.connection, (c) -> c.source).toOne(TargetModel.class,
					(t) -> t.connection, (c) -> c.target, (s, t, ctx) -> new MappingConnection(s, t),
					(ctx) -> ctx.getModel().remove());
		});

		// Create the parent
		Parent parent = module.createParent();

		// Return the input (allowing activation)
		return new ValueInput() {

			@Override
			public Node getNode() {
				return parent;
			}

			@Override
			public void activate() {
				// Load the root model
				module.loadRootModel(new MappingModel());
			}
		};
	}

	/**
	 * Root mapping {@link Model}.
	 */
	private static class MappingModel extends AbstractModel {

		public List<SourceModel> getSources() {
			return null;
		}
	}

	/**
	 * Source {@link Model}.
	 */
	private static class SourceModel extends AbstractModel {

		/**
		 * Label.
		 */
		private final String label;

		/**
		 * {@link MappingConnection}.
		 */
		private MappingConnection connection = null;

		/**
		 * Instantiate.
		 * 
		 * @param label
		 *            Label.
		 * @param x
		 *            X location.
		 * @param y
		 *            Y location.
		 */
		private SourceModel(String label, int x, int y) {
			this.label = label;
			this.setX(x);
			this.setY(y);
		}
	}

	/**
	 * Target {@link Model}.
	 */
	private static class TargetModel extends AbstractModel {

		/**
		 * Label.
		 */
		public final String label;

		/**
		 * {@link MappingConnection}.
		 */
		private MappingConnection connection = null;

		/**
		 * Instantiate.
		 * 
		 * @param label
		 *            Label.
		 * @param x
		 *            X location.
		 * @param y
		 *            Y location.
		 */
		private TargetModel(String label, int x, int y) {
			this.label = label;
			this.setX(x);
			this.setY(y);
		}
	}

	/**
	 * Mapping {@link ConnectionModel}.
	 */
	private static class MappingConnection extends AbstractModel implements ConnectionModel {

		/**
		 * {@link SourceModel}.
		 */
		private final SourceModel source;

		/**
		 * {@link TargetModel}.
		 */
		private final TargetModel target;

		/**
		 * Instantiate.
		 * 
		 * @param source
		 *            {@link SourceModel}.
		 * @param target
		 *            {@link TargetModel}.
		 */
		private MappingConnection(SourceModel source, TargetModel target) {
			this.source = source;
			this.target = target;
		}

		/*
		 * ============== ConnectionModel =================
		 */

		@Override
		public boolean isRemovable() {
			return true;
		}

		@Override
		public void connect() {
			this.source.connection = this;
			this.target.connection = this;
		}

		@Override
		public void remove() {
			this.source.connection = null;
			this.target.connection = null;
		}
	}

}