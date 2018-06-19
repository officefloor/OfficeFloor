/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.section.SectionManagedObjectFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel.SectionManagedObjectEvent;

/**
 * {@link EditPart} for the {@link SectionManagedObjectModel}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionManagedObjectEditPart extends
		AbstractOfficeFloorEditPart<SectionManagedObjectModel, SectionManagedObjectEvent, SectionManagedObjectFigure>
		implements SectionManagedObjectFigureContext {

	/*
	 * =============== AbstractOfficeFloorEditPart ============================
	 */

	@Override
	protected SectionManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createSectionManagedObjectFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getSectionManagedObjectDependencies());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getSectionManagedObjectSource());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSubSectionObjects());
		models.addAll(this.getCastedModel().getDependentSectionManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<SectionManagedObjectModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<SectionChanges, SectionManagedObjectModel>() {
			@Override
			public String getInitialValue() {
				return SectionManagedObjectEditPart.this.getCastedModel().getSectionManagedObjectName();
			}

			@Override
			public IFigure getLocationFigure() {
				return SectionManagedObjectEditPart.this.getOfficeFloorFigure().getSectionManagedObjectNameFigure();
			}

			@Override
			public Change<SectionManagedObjectModel> createChange(SectionChanges changes,
					SectionManagedObjectModel target, String newValue) {
				return changes.renameSectionManagedObject(target, newValue);
			}
		});
	}

	@Override
	protected Class<SectionManagedObjectEvent> getPropertyChangeEventType() {
		return SectionManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(SectionManagedObjectEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SECTION_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure()
					.setSectionManagedObjectName(this.getCastedModel().getSectionManagedObjectName());
			break;
		case CHANGE_MANAGED_OBJECT_SCOPE:
			this.getOfficeFloorFigure().setManagedObjectScope(this.getManagedObjectScope());
			break;
		case ADD_SECTION_MANAGED_OBJECT_DEPENDENCY:
		case REMOVE_SECTION_MANAGED_OBJECT_DEPENDENCY:
			this.refreshChildren();
			break;
		case CHANGE_SECTION_MANAGED_OBJECT_SOURCE:
			this.refreshSourceConnections();
			break;
		case ADD_SUB_SECTION_OBJECT:
		case REMOVE_SUB_SECTION_OBJECT:
		case ADD_DEPENDENT_SECTION_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_SECTION_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= SectionManagedObjectFigureContext ===============
	 */

	@Override
	public String getSectionManagedObjectName() {
		return this.getCastedModel().getSectionManagedObjectName();
	}

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		// Return the scope
		String scopeName = this.getCastedModel().getManagedObjectScope();
		if (SectionChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.PROCESS;
		} else if (SectionChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.THREAD;
		} else if (SectionChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.FUNCTION;
		} else {
			// Unknown scope
			return null;
		}
	}

}