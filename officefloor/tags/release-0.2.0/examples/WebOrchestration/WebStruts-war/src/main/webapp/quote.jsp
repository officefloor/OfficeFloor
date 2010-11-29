<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Quote</h1>
		<br />
		<p>Customer: <s:property value="quote.customer.name" /></p>
		<table id="items">
			<tr>
				<th>Product</th>
				<th>Price per item</th>
				<th>Quantity</th>
				<th>Line Price</th>
			</tr>
			<s:iterator value="quote.quoteItems">
				<tr>						
					<td><s:property value="product.name"/></td>
					<td><s:property value="productPrice"/></td>
					<td><s:property value="quantity"/></td>
					<td><s:property value="quoteItemPrice"/></td>
				</tr>
			</s:iterator>
		</table>
		<s:form action="purchaseQuote">
			<input type="hidden" name="quoteId" value="<s:property value='quote.quoteId'/>" />
			<s:submit name="purchase" value="Purchase" />
		</s:form>
	</body>
</html>