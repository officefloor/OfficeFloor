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
public class ServiceManagedObjectModel extends AbstractModel implements ItemModel<ServiceManagedObjectModel> {

    public static enum ServiceManagedObjectEvent {
     CHANGE_SERVICE_MANAGED_OBJECT_NAME, CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME, ADD_PROPERTY, REMOVE_PROPERTY, ADD_PROPERTY_FILE, REMOVE_PROPERTY_FILE, ADD_OBJECT_TYPE, REMOVE_OBJECT_TYPE
    }

    /**
     * Default constructor.
     */
    public ServiceManagedObjectModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public ServiceManagedObjectModel(
      String serviceManagedObjectName
    , String managedObjectSourceClassName
    ) {
        this.serviceManagedObjectName = serviceManagedObjectName;
        this.managedObjectSourceClassName = managedObjectSourceClassName;
    }

    /**
     * Convenience constructor.
     */
    public ServiceManagedObjectModel(
      String serviceManagedObjectName
    , String managedObjectSourceClassName
    , PropertyModel[] property
    , PropertyFileModel[] propertyFile
    , ServiceManagedObjectTypeModel[] objectType
    ) {
        this.serviceManagedObjectName = serviceManagedObjectName;
        this.managedObjectSourceClassName = managedObjectSourceClassName;
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
        if (objectType != null) {
            for (ServiceManagedObjectTypeModel model : objectType) {
                this.objectType.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public ServiceManagedObjectModel(
      String serviceManagedObjectName
    , String managedObjectSourceClassName
    , PropertyModel[] property
    , PropertyFileModel[] propertyFile
    , ServiceManagedObjectTypeModel[] objectType
    , int x
    , int y
    ) {
        this.serviceManagedObjectName = serviceManagedObjectName;
        this.managedObjectSourceClassName = managedObjectSourceClassName;
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
        if (objectType != null) {
            for (ServiceManagedObjectTypeModel model : objectType) {
                this.objectType.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Service managed object name.
     */
    private String serviceManagedObjectName;

    public String getServiceManagedObjectName() {
        return this.serviceManagedObjectName;
    }

    public void setServiceManagedObjectName(String serviceManagedObjectName) {
        String oldValue = this.serviceManagedObjectName;
        this.serviceManagedObjectName = serviceManagedObjectName;
        this.changeField(oldValue, this.serviceManagedObjectName, ServiceManagedObjectEvent.CHANGE_SERVICE_MANAGED_OBJECT_NAME);
    }

    /**
     * Managed object source class name.
     */
    private String managedObjectSourceClassName;

    public String getManagedObjectSourceClassName() {
        return this.managedObjectSourceClassName;
    }

    public void setManagedObjectSourceClassName(String managedObjectSourceClassName) {
        String oldValue = this.managedObjectSourceClassName;
        this.managedObjectSourceClassName = managedObjectSourceClassName;
        this.changeField(oldValue, this.managedObjectSourceClassName, ServiceManagedObjectEvent.CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME);
    }


    /**
     * Property.
     */
    private List<PropertyModel> property = new LinkedList<PropertyModel>();

    public List<PropertyModel> getProperties() {
        return this.property;
    }

    public void addProperty(PropertyModel property) {
        this.addItemToList(property, this.property, ServiceManagedObjectEvent.ADD_PROPERTY);
    }

    public void removeProperty(PropertyModel property) {
        this.removeItemFromList(property, this.property, ServiceManagedObjectEvent.REMOVE_PROPERTY);
    }

    /**
     * Property file.
     */
    private List<PropertyFileModel> propertyFile = new LinkedList<PropertyFileModel>();

    public List<PropertyFileModel> getPropertyFiles() {
        return this.propertyFile;
    }

    public void addPropertyFile(PropertyFileModel propertyFile) {
        this.addItemToList(propertyFile, this.propertyFile, ServiceManagedObjectEvent.ADD_PROPERTY_FILE);
    }

    public void removePropertyFile(PropertyFileModel propertyFile) {
        this.removeItemFromList(propertyFile, this.propertyFile, ServiceManagedObjectEvent.REMOVE_PROPERTY_FILE);
    }

    /**
     * Object type.
     */
    private List<ServiceManagedObjectTypeModel> objectType = new LinkedList<ServiceManagedObjectTypeModel>();

    public List<ServiceManagedObjectTypeModel> getObjectTypes() {
        return this.objectType;
    }

    public void addObjectType(ServiceManagedObjectTypeModel objectType) {
        this.addItemToList(objectType, this.objectType, ServiceManagedObjectEvent.ADD_OBJECT_TYPE);
    }

    public void removeObjectType(ServiceManagedObjectTypeModel objectType) {
        this.removeItemFromList(objectType, this.objectType, ServiceManagedObjectEvent.REMOVE_OBJECT_TYPE);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<ServiceManagedObjectModel> removeConnections() {
        RemoveConnectionsAction<ServiceManagedObjectModel> _action = new RemoveConnectionsAction<ServiceManagedObjectModel>(this);
        return _action;
    }
}
