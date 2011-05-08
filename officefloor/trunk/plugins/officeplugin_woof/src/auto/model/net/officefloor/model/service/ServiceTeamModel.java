/*
 * 
 */
package net.officefloor.model.service;

import java.util.List;
import java.util.LinkedList;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class ServiceTeamModel extends AbstractModel implements ItemModel<ServiceTeamModel> {

    public static enum ServiceTeamEvent {
     CHANGE_SERVICE_TEAM_NAME, CHANGE_TEAM_SOURCE_CLASS_NAME, ADD_PROPERTY, REMOVE_PROPERTY, ADD_PROPERTY_FILE, REMOVE_PROPERTY_FILE
    }

    /**
     * Default constructor.
     */
    public ServiceTeamModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public ServiceTeamModel(
      String serviceTeamName
    , String teamSourceClassName
    ) {
        this.serviceTeamName = serviceTeamName;
        this.teamSourceClassName = teamSourceClassName;
    }

    /**
     * Convenience constructor.
     */
    public ServiceTeamModel(
      String serviceTeamName
    , String teamSourceClassName
    , PropertyModel[] property
    , PropertyFileModel[] propertyFile
    ) {
        this.serviceTeamName = serviceTeamName;
        this.teamSourceClassName = teamSourceClassName;
        if (property != null) {
            for (PropertyModel model : property) {
                this.property.add(model);
            }
        }
        if (propertyFile != null) {
            for (PropertyFileModel model : propertyFile) {
                this.propertyFile.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public ServiceTeamModel(
      String serviceTeamName
    , String teamSourceClassName
    , PropertyModel[] property
    , PropertyFileModel[] propertyFile
    , int x
    , int y
    ) {
        this.serviceTeamName = serviceTeamName;
        this.teamSourceClassName = teamSourceClassName;
        if (property != null) {
            for (PropertyModel model : property) {
                this.property.add(model);
            }
        }
        if (propertyFile != null) {
            for (PropertyFileModel model : propertyFile) {
                this.propertyFile.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Service team name.
     */
    private String serviceTeamName;

    public String getServiceTeamName() {
        return this.serviceTeamName;
    }

    public void setServiceTeamName(String serviceTeamName) {
        String oldValue = this.serviceTeamName;
        this.serviceTeamName = serviceTeamName;
        this.changeField(oldValue, this.serviceTeamName, ServiceTeamEvent.CHANGE_SERVICE_TEAM_NAME);
    }

    /**
     * Team source class name.
     */
    private String teamSourceClassName;

    public String getTeamSourceClassName() {
        return this.teamSourceClassName;
    }

    public void setTeamSourceClassName(String teamSourceClassName) {
        String oldValue = this.teamSourceClassName;
        this.teamSourceClassName = teamSourceClassName;
        this.changeField(oldValue, this.teamSourceClassName, ServiceTeamEvent.CHANGE_TEAM_SOURCE_CLASS_NAME);
    }


    /**
     * Property.
     */
    private List<PropertyModel> property = new LinkedList<PropertyModel>();

    public List<PropertyModel> getProperties() {
        return this.property;
    }

    public void addProperty(PropertyModel property) {
        this.addItemToList(property, this.property, ServiceTeamEvent.ADD_PROPERTY);
    }

    public void removeProperty(PropertyModel property) {
        this.removeItemFromList(property, this.property, ServiceTeamEvent.REMOVE_PROPERTY);
    }

    /**
     * Property file.
     */
    private List<PropertyFileModel> propertyFile = new LinkedList<PropertyFileModel>();

    public List<PropertyFileModel> getPropertyFiles() {
        return this.propertyFile;
    }

    public void addPropertyFile(PropertyFileModel propertyFile) {
        this.addItemToList(propertyFile, this.propertyFile, ServiceTeamEvent.ADD_PROPERTY_FILE);
    }

    public void removePropertyFile(PropertyFileModel propertyFile) {
        this.removeItemFromList(propertyFile, this.propertyFile, ServiceTeamEvent.REMOVE_PROPERTY_FILE);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<ServiceTeamModel> removeConnections() {
        RemoveConnectionsAction<ServiceTeamModel> _action = new RemoveConnectionsAction<ServiceTeamModel>(this);
        return _action;
    }
}
