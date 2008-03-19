package public_.productprice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import public_.product.Product;
import public_.purchaseorderlineitem.PurchaseOrderLineItem;

public class ProductPriceRepository {

    /**
     * Retrieves {@link ProductPrice} by column PRODUCT_ID.
     */
    public List<ProductPrice> retrieveByProductId(Integer productId, Connection connection) throws SQLException {
        return this.retrieveList(connection, "SELECT * FROM PRODUCT_PRICE WHERE PRODUCT_ID = ?", productId);
    }

    /**
     * Retrieves {@link ProductPrice} by column PRODUCT_ID, QUANTITY.
     */
    public ProductPrice retrieveByProductIdQuantity(ProductPriceIndexProductIdQuantity access, Connection connection) throws SQLException {
        return this.retrieve(connection, "SELECT * FROM PRODUCT_PRICE WHERE PRODUCT_ID = ? AND QUANTITY = ?", access.getProductId(), access.getQuantity());
    }

    /**
     * Retrieves {@link ProductPrice} by {@link Product}.
     */
    public List<ProductPrice> retrieveFromProduct(Product table, Connection connection) throws SQLException {
        return this.retrieveList(connection, "SELECT * FROM PRODUCT_PRICE WHERE PRODUCT_ID = ?", table.getProductId());
    }

    /**
     * Retrieves {@link ProductPrice} by {@link PurchaseOrderLineItem}.
     */
    public ProductPrice retrieveFromPurchaseOrderLineItem(PurchaseOrderLineItem table, Connection connection) throws SQLException {
        return this.retrieve(connection, "SELECT * FROM PRODUCT_PRICE WHERE PRODUCT_ID = ? AND QUANTITY = ?", table.getProductId(), table.getQuantity());
    }

    /**
     * Retrieves a single {@link ProductPrice}.
     */
    public ProductPrice retrieve(Connection connection, String sql, Object... parameters) throws SQLException {
        // Prepare statement for execution
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject((i + 1), parameters[i]);
            }
            // Return result
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ProductPrice bean = new ProductPrice();
                bean.load(resultSet);
                return bean;
            } else {
                return null;
            }
        } finally {
            statement.close();
        }
    }

    /**
     * Retrieves list of {@link ProductPrice} instances.
     */
    public List<ProductPrice> retrieveList(Connection connection, String sql, Object... parameters) throws SQLException {
        // Prepare statement for execution
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject((i + 1), parameters[i]);
            }
            // Return result
            ResultSet resultSet = statement.executeQuery();
            List<ProductPrice> beans = new LinkedList<ProductPrice>();
            while (resultSet.next()) {
                ProductPrice bean = new ProductPrice();
                bean.load(resultSet);
                beans.add(bean);
            }
            // Return results
            return beans;
        } finally {
            statement.close();
        }
    }

}
