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
public class WoofModel extends AbstractModel implements ItemModel<WoofModel> {

    public static enum WoofEvent {
     ADD_WOOF_TEMPLATE, REMOVE_WOOF_TEMPLATE, ADD_WOOF_SECTION, REMOVE_WOOF_SECTION, ADD_WOOF_RESOURCE, REMOVE_WOOF_RESOURCE, ADD_WOOF_EXCEPTION, REMOVE_WOOF_EXCEPTION
    }

    /**
     * Default constructor.
     */
    public WoofModel() {
    }

    /**
     * Convenience constructor.
     */
    public WoofModel(
      WoofTemplateModel[] woofTemplate
    , WoofSectionModel[] woofSection
    , WoofResourceModel[] woofResource
    , WoofExceptionModel[] woofException
    ) {
        if (woofTemplate != null) {
            for (WoofTemplateModel model : woofTemplate) {
                this.woofTemplate.add(model);
            }
        }
        if (woofSection != null) {
            for (WoofSectionModel model : woofSection) {
                this.woofSection.add(model);
            }
        }
        if (woofResource != null) {
            for (WoofResourceModel model : woofResource) {
                this.woofResource.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionModel model : woofException) {
                this.woofException.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofModel(
      WoofTemplateModel[] woofTemplate
    , WoofSectionModel[] woofSection
    , WoofResourceModel[] woofResource
    , WoofExceptionModel[] woofException
    , int x
    , int y
    ) {
        if (woofTemplate != null) {
            for (WoofTemplateModel model : woofTemplate) {
                this.woofTemplate.add(model);
            }
        }
        if (woofSection != null) {
            for (WoofSectionModel model : woofSection) {
                this.woofSection.add(model);
            }
        }
        if (woofResource != null) {
            for (WoofResourceModel model : woofResource) {
                this.woofResource.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionModel model : woofException) {
                this.woofException.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }


    /**
     * Woof template.
     */
    private List<WoofTemplateModel> woofTemplate = new LinkedList<WoofTemplateModel>();

    public List<WoofTemplateModel> getWoofTemplates() {
        return this.woofTemplate;
    }

    public void addWoofTemplate(WoofTemplateModel woofTemplate) {
        this.addItemToList(woofTemplate, this.woofTemplate, WoofEvent.ADD_WOOF_TEMPLATE);
    }

    public void removeWoofTemplate(WoofTemplateModel woofTemplate) {
        this.removeItemFromList(woofTemplate, this.woofTemplate, WoofEvent.REMOVE_WOOF_TEMPLATE);
    }

    /**
     * Woof section.
     */
    private List<WoofSectionModel> woofSection = new LinkedList<WoofSectionModel>();

    public List<WoofSectionModel> getWoofSections() {
        return this.woofSection;
    }

    public void addWoofSection(WoofSectionModel woofSection) {
        this.addItemToList(woofSection, this.woofSection, WoofEvent.ADD_WOOF_SECTION);
    }

    public void removeWoofSection(WoofSectionModel woofSection) {
        this.removeItemFromList(woofSection, this.woofSection, WoofEvent.REMOVE_WOOF_SECTION);
    }

    /**
     * Woof resource.
     */
    private List<WoofResourceModel> woofResource = new LinkedList<WoofResourceModel>();

    public List<WoofResourceModel> getWoofResources() {
        return this.woofResource;
    }

    public void addWoofResource(WoofResourceModel woofResource) {
        this.addItemToList(woofResource, this.woofResource, WoofEvent.ADD_WOOF_RESOURCE);
    }

    public void removeWoofResource(WoofResourceModel woofResource) {
        this.removeItemFromList(woofResource, this.woofResource, WoofEvent.REMOVE_WOOF_RESOURCE);
    }

    /**
     * Woof exception.
     */
    private List<WoofExceptionModel> woofException = new LinkedList<WoofExceptionModel>();

    public List<WoofExceptionModel> getWoofExceptions() {
        return this.woofException;
    }

    public void addWoofException(WoofExceptionModel woofException) {
        this.addItemToList(woofException, this.woofException, WoofEvent.ADD_WOOF_EXCEPTION);
    }

    public void removeWoofException(WoofExceptionModel woofException) {
        this.removeItemFromList(woofException, this.woofException, WoofEvent.REMOVE_WOOF_EXCEPTION);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofModel> removeConnections() {
        RemoveConnectionsAction<WoofModel> _action = new RemoveConnectionsAction<WoofModel>(this);
        return _action;
    }
}
