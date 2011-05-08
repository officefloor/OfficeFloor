/*
 * 
 */
package net.officefloor.model.service;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class PropertyFileModel extends AbstractModel implements ItemModel<PropertyFileModel> {

    public static enum PropertyFileEvent {
     CHANGE_PATH
    }

    /**
     * Default constructor.
     */
    public PropertyFileModel() {
    }

    /**
     * Convenience constructor.
     */
    public PropertyFileModel(
      String path
    ) {
        this.path = path;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public PropertyFileModel(
      String path
    , int x
    , int y
    ) {
        this.path = path;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Path.
     */
    private String path;

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        String oldValue = this.path;
        this.path = path;
        this.changeField(oldValue, this.path, PropertyFileEvent.CHANGE_PATH);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<PropertyFileModel> removeConnections() {
        RemoveConnectionsAction<PropertyFileModel> _action = new RemoveConnectionsAction<PropertyFileModel>(this);
        return _action;
    }
}
