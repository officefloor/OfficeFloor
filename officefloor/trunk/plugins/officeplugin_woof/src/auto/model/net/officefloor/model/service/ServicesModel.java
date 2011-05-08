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
public class ServicesModel extends AbstractModel implements ItemModel<ServicesModel> {

    public static enum ServicesEvent {
     ADD_SERVICE_MANAGED_OBJECT, REMOVE_SERVICE_MANAGED_OBJECT, ADD_SERVICE_TEAM, REMOVE_SERVICE_TEAM
    }

    /**
     * Default constructor.
     */
    public ServicesModel() {
    }

    /**
     * Convenience constructor.
     */
    public ServicesModel(
      ServiceManagedObjectModel[] serviceManagedObject
    , ServiceTeamModel[] serviceTeam
    ) {
        if (serviceManagedObject != null) {
            for (ServiceManagedObjectModel model : serviceManagedObject) {
                this.serviceManagedObject.add(model);
            }
        }
        if (serviceTeam != null) {
            for (ServiceTeamModel model : serviceTeam) {
                this.serviceTeam.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public ServicesModel(
      ServiceManagedObjectModel[] serviceManagedObject
    , ServiceTeamModel[] serviceTeam
    , int x
    , int y
    ) {
        if (serviceManagedObject != null) {
            for (ServiceManagedObjectModel model : serviceManagedObject) {
                this.serviceManagedObject.add(model);
            }
        }
        if (serviceTeam != null) {
            for (ServiceTeamModel model : serviceTeam) {
                this.serviceTeam.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }


    /**
     * Service managed object.
     */
    private List<ServiceManagedObjectModel> serviceManagedObject = new LinkedList<ServiceManagedObjectModel>();

    public List<ServiceManagedObjectModel> getServiceManagedObjects() {
        return this.serviceManagedObject;
    }

    public void addServiceManagedObject(ServiceManagedObjectModel serviceManagedObject) {
        this.addItemToList(serviceManagedObject, this.serviceManagedObject, ServicesEvent.ADD_SERVICE_MANAGED_OBJECT);
    }

    public void removeServiceManagedObject(ServiceManagedObjectModel serviceManagedObject) {
        this.removeItemFromList(serviceManagedObject, this.serviceManagedObject, ServicesEvent.REMOVE_SERVICE_MANAGED_OBJECT);
    }

    /**
     * Service team.
     */
    private List<ServiceTeamModel> serviceTeam = new LinkedList<ServiceTeamModel>();

    public List<ServiceTeamModel> getServiceTeams() {
        return this.serviceTeam;
    }

    public void addServiceTeam(ServiceTeamModel serviceTeam) {
        this.addItemToList(serviceTeam, this.serviceTeam, ServicesEvent.ADD_SERVICE_TEAM);
    }

    public void removeServiceTeam(ServiceTeamModel serviceTeam) {
        this.removeItemFromList(serviceTeam, this.serviceTeam, ServicesEvent.REMOVE_SERVICE_TEAM);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<ServicesModel> removeConnections() {
        RemoveConnectionsAction<ServicesModel> _action = new RemoveConnectionsAction<ServicesModel>(this);
        return _action;
    }
}
