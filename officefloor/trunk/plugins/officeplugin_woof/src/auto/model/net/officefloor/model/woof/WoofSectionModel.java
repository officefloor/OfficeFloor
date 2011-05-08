/*
 * 
 */
package net.officefloor.model.woof;

import java.util.List;
import java.util.LinkedList;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofSectionModel extends AbstractModel implements ItemModel<WoofSectionModel> {

    public static enum WoofSectionEvent {
     CHANGE_WOOF_SECTION_NAME, CHANGE_SECTION_SOURCE_CLASS_NAME, CHANGE_SECTION_LOCATION, ADD_PROPERTY, REMOVE_PROPERTY, ADD_INPUT, REMOVE_INPUT, ADD_OUTPUT, REMOVE_OUTPUT
    }

    /**
     * Default constructor.
     */
    public WoofSectionModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionModel(
      String woofSectionName
    , String sectionSourceClassName
    , String sectionLocation
    ) {
        this.woofSectionName = woofSectionName;
        this.sectionSourceClassName = sectionSourceClassName;
        this.sectionLocation = sectionLocation;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionModel(
      String woofSectionName
    , String sectionSourceClassName
    , String sectionLocation
    , PropertyModel[] property
    , WoofSectionInputModel[] input
    , WoofSectionOutputModel[] output
    ) {
        this.woofSectionName = woofSectionName;
        this.sectionSourceClassName = sectionSourceClassName;
        this.sectionLocation = sectionLocation;
        if (property != null) {
            for (PropertyModel model : property) {
                this.property.add(model);
            }
        }
        if (input != null) {
            for (WoofSectionInputModel model : input) {
                this.input.add(model);
            }
        }
        if (output != null) {
            for (WoofSectionOutputModel model : output) {
                this.output.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionModel(
      String woofSectionName
    , String sectionSourceClassName
    , String sectionLocation
    , PropertyModel[] property
    , WoofSectionInputModel[] input
    , WoofSectionOutputModel[] output
    , int x
    , int y
    ) {
        this.woofSectionName = woofSectionName;
        this.sectionSourceClassName = sectionSourceClassName;
        this.sectionLocation = sectionLocation;
        if (property != null) {
            for (PropertyModel model : property) {
                this.property.add(model);
            }
        }
        if (input != null) {
            for (WoofSectionInputModel model : input) {
                this.input.add(model);
            }
        }
        if (output != null) {
            for (WoofSectionOutputModel model : output) {
                this.output.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof section name.
     */
    private String woofSectionName;

    public String getWoofSectionName() {
        return this.woofSectionName;
    }

    public void setWoofSectionName(String woofSectionName) {
        String oldValue = this.woofSectionName;
        this.woofSectionName = woofSectionName;
        this.changeField(oldValue, this.woofSectionName, WoofSectionEvent.CHANGE_WOOF_SECTION_NAME);
    }

    /**
     * Section source class name.
     */
    private String sectionSourceClassName;

    public String getSectionSourceClassName() {
        return this.sectionSourceClassName;
    }

    public void setSectionSourceClassName(String sectionSourceClassName) {
        String oldValue = this.sectionSourceClassName;
        this.sectionSourceClassName = sectionSourceClassName;
        this.changeField(oldValue, this.sectionSourceClassName, WoofSectionEvent.CHANGE_SECTION_SOURCE_CLASS_NAME);
    }

    /**
     * Section location.
     */
    private String sectionLocation;

    public String getSectionLocation() {
        return this.sectionLocation;
    }

    public void setSectionLocation(String sectionLocation) {
        String oldValue = this.sectionLocation;
        this.sectionLocation = sectionLocation;
        this.changeField(oldValue, this.sectionLocation, WoofSectionEvent.CHANGE_SECTION_LOCATION);
    }


    /**
     * Property.
     */
    private List<PropertyModel> property = new LinkedList<PropertyModel>();

    public List<PropertyModel> getProperties() {
        return this.property;
    }

    public void addProperty(PropertyModel property) {
        this.addItemToList(property, this.property, WoofSectionEvent.ADD_PROPERTY);
    }

    public void removeProperty(PropertyModel property) {
        this.removeItemFromList(property, this.property, WoofSectionEvent.REMOVE_PROPERTY);
    }

    /**
     * Input.
     */
    private List<WoofSectionInputModel> input = new LinkedList<WoofSectionInputModel>();

    public List<WoofSectionInputModel> getInputs() {
        return this.input;
    }

    public void addInput(WoofSectionInputModel input) {
        this.addItemToList(input, this.input, WoofSectionEvent.ADD_INPUT);
    }

    public void removeInput(WoofSectionInputModel input) {
        this.removeItemFromList(input, this.input, WoofSectionEvent.REMOVE_INPUT);
    }

    /**
     * Output.
     */
    private List<WoofSectionOutputModel> output = new LinkedList<WoofSectionOutputModel>();

    public List<WoofSectionOutputModel> getOutputs() {
        return this.output;
    }

    public void addOutput(WoofSectionOutputModel output) {
        this.addItemToList(output, this.output, WoofSectionEvent.ADD_OUTPUT);
    }

    public void removeOutput(WoofSectionOutputModel output) {
        this.removeItemFromList(output, this.output, WoofSectionEvent.REMOVE_OUTPUT);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofSectionModel> removeConnections() {
        RemoveConnectionsAction<WoofSectionModel> _action = new RemoveConnectionsAction<WoofSectionModel>(this);
        return _action;
    }
}
