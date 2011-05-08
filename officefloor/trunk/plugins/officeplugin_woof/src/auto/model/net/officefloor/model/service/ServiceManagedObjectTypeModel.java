/*
 * 
 */
package net.officefloor.model.service;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class ServiceManagedObjectTypeModel extends AbstractModel implements ItemModel<ServiceManagedObjectTypeModel> {

    public static enum ServiceManagedObjectTypeEvent {
     CHANGE_OBJECT_TYPE
    }

    /**
     * Default constructor.
     */
    public ServiceManagedObjectTypeModel() {
    }

    /**
     * Convenience constructor.
     */
    public ServiceManagedObjectTypeModel(
      String objectType
    ) {
        this.objectType = objectType;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public ServiceManagedObjectTypeModel(
      String objectType
    , int x
    , int y
    ) {
        this.objectType = objectType;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Object type.
     */
    private String objectType;

    public String getObjectType() {
        return this.objectType;
    }

    public void setObjectType(String objectType) {
        String oldValue = this.objectType;
        this.objectType = objectType;
        this.changeField(oldValue, this.objectType, ServiceManagedObjectTypeEvent.CHANGE_OBJECT_TYPE);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<ServiceManagedObjectTypeModel> removeConnections() {
        RemoveConnectionsAction<ServiceManagedObjectTypeModel> _action = new RemoveConnectionsAction<ServiceManagedObjectTypeModel>(this);
        return _action;
    }
}
