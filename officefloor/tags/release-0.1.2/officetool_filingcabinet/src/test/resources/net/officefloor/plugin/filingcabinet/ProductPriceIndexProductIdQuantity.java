package net.officefloor.packageprefix.public_.productprice;

public class ProductPriceIndexProductIdQuantity {

    /**
     * Column PRODUCT_ID.
     */
    private Integer productId;

    /**
     * Column QUANTITY.
     */
    private Integer quantity;

    /**
     * Initialise.
     */
    public ProductPriceIndexProductIdQuantity(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
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

}
