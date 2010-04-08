<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Invoice</h1>
		<br />
		<p>Customer: <s:property value="invoice.customer.name" /></p>
		<table id="lineItems">
			<tr>
				<th>Product</th>
				<th>Price per item</th>
				<th>Quantity</th>
				<th>Allocated</th>
				<th>Line Price</th>
			</tr>
			<s:iterator value="invoice.invoiceLineItems">
				<tr>						
					<td><s:property value="product.name"/></td>
					<td><s:property value="productPrice"/></td>
					<td><s:property value="quantity"/></td>
					<td><s:property value="productAllocation.quantityAllocated"/></td>
					<td><s:property value="invoiceLineItemPrice"/></td>
				</tr>
			</s:iterator>
		</table>
		<a href="<s:url action='index'/>">Home</a>
	</body>
</html>