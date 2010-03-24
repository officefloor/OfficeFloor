<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Shopping Cart</h1>
		<br />
		<p>Customer: <s:property value="shoppingCart.customer.name" /></p>
		<table id="items">
			<tr>
				<th>Product</th>
				<th>Quantity</th>
				<th>Price per item</th>
			</tr>
			<s:iterator value="shoppingCart.shoppingCartItems">
				<tr>						
					<td><s:property value="product.name"/></td>
					<td><s:property value="quantity"/></td>
					<td><s:property value="product.price"/></td>
				</tr>
			</s:iterator>
		</table>
		<a href="<s:url action='selectProducts'/>">Continue Shopping</a>
		<a href="<s:url action='quote'/>">Quote</a>
		<s:form action="purchaseShoppingCart">
			<s:submit name="purchase" value="Purchase" />
        </s:form>
	</body>
</html>