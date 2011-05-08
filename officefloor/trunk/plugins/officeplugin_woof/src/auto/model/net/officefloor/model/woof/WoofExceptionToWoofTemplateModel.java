/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofExceptionToWoofTemplateModel extends AbstractModel implements ConnectionModel {

    public static enum WoofExceptionToWoofTemplateEvent {
     CHANGE_TEMPLATE_NAME, CHANGE_WOOF_EXCEPTION, CHANGE_WOOF_TEMPLATE
    }

    /**
     * Default constructor.
     */
    public WoofExceptionToWoofTemplateModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofExceptionToWoofTemplateModel(
      String templateName
    ) {
        this.templateName = templateName;
    }

    /**
     * Convenience constructor.
     */
    public WoofExceptionToWoofTemplateModel(
      String templateName
    , WoofExceptionModel woofException
    , WoofTemplateModel woofTemplate
    ) {
        this.templateName = templateName;
        this.woofException = woofException;
        this.woofTemplate = woofTemplate;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofExceptionToWoofTemplateModel(
      String templateName
    , WoofExceptionModel woofException
    , WoofTemplateModel woofTemplate
    , int x
    , int y
    ) {
        this.templateName = templateName;
        this.woofException = woofException;
        this.woofTemplate = woofTemplate;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Template name.
     */
    private String templateName;

    public String getTemplateName() {
        return this.templateName;
    }

    public void setTemplateName(String templateName) {
        String oldValue = this.templateName;
        this.templateName = templateName;
        this.changeField(oldValue, this.templateName, WoofExceptionToWoofTemplateEvent.CHANGE_TEMPLATE_NAME);
    }

    /**
     * Woof exception.
     */
    private WoofExceptionModel woofException;

    public WoofExceptionModel getWoofException() {
        return this.woofException;
    }

    public void setWoofException(WoofExceptionModel woofException) {
        WoofExceptionModel oldValue = this.woofException;
        this.woofException = woofException;
        this.changeField(oldValue, this.woofException, WoofExceptionToWoofTemplateEvent.CHANGE_WOOF_EXCEPTION);
    }

    /**
     * Woof template.
     */
    private WoofTemplateModel woofTemplate;

    public WoofTemplateModel getWoofTemplate() {
        return this.woofTemplate;
    }

    public void setWoofTemplate(WoofTemplateModel woofTemplate) {
        WoofTemplateModel oldValue = this.woofTemplate;
        this.woofTemplate = woofTemplate;
        this.changeField(oldValue, this.woofTemplate, WoofExceptionToWoofTemplateEvent.CHANGE_WOOF_TEMPLATE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofException.setWoofTemplate(this);
        this.woofTemplate.addWoofException(this);
    }

    public void remove() {
        this.woofException.setWoofTemplate(null);
        this.woofTemplate.removeWoofException(this);
    }

}
