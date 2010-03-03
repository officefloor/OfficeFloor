package net.officefloor.packageprefix.public_.productprice;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.officefloor.packageprefix.public_.product.Product;
import net.officefloor.packageprefix.public_.purchaseorderlineitem.PurchaseOrderLineItem;

public class ProductPrice {

    /**
     * Column PRICE.
     */
    private Float price;

    /**
     * Column PRODUCT_ID.
     */
    private Integer productId;

    /**
     * Column QUANTITY.
     */
    private Integer quantity;

    /**
     * Obtains value for column PRICE.
     */
    public Float getPrice(){
        return this.price;
    }

    /**
     * Specifies value for column PRICE.
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * Obtains value for column PRODUCT_ID.
     */
    public Integer getProductId(){
        return this.productId;
    }

    /**
     * Specifies value for column PRODUCT_ID.
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    /**
     * Obtains value for column QUANTITY.
     */
    public Integer getQuantity(){
        return this.quantity;
    }

    /**
     * Specifies value for column QUANTITY.
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Links to {@link Product}.
     */
    public void linkToProduct(Product table) {
        this.productId = table.getProductId();
    }

    /**
     * Links to {@link PurchaseOrderLineItem}.
     */
    public void linkToPurchaseOrderLineItem(PurchaseOrderLineItem table) {
        this.productId = table.getProductId();
        this.quantity = table.getQuantity();
    }

    /**
     * Loads state from the {@link ResultSet}.
     */
    public void load(ResultSet resultSet) throws SQLException {
        this.productId = (Integer) resultSet.getObject("PRODUCT_ID");
        this.quantity = (Integer) resultSet.getObject("QUANTITY");
        this.price = (Float) resultSet.getObject("PRICE");
    }

}
