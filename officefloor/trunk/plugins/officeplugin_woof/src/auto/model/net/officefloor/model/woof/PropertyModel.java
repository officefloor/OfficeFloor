/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class PropertyModel extends AbstractModel implements ItemModel<PropertyModel> {

    public static enum PropertyEvent {
     CHANGE_NAME, CHANGE_VALUE
    }

    /**
     * Default constructor.
     */
    public PropertyModel() {
    }

    /**
     * Convenience constructor.
     */
    public PropertyModel(
      String name
    , String value
    ) {
        this.name = name;
        this.value = value;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public PropertyModel(
      String name
    , String value
    , int x
    , int y
    ) {
        this.name = name;
        this.value = value;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Name.
     */
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        this.changeField(oldValue, this.name, PropertyEvent.CHANGE_NAME);
    }

    /**
     * Value.
     */
    private String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;
        this.changeField(oldValue, this.value, PropertyEvent.CHANGE_VALUE);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<PropertyModel> removeConnections() {
        RemoveConnectionsAction<PropertyModel> _action = new RemoveConnectionsAction<PropertyModel>(this);
        return _action;
    }
}
