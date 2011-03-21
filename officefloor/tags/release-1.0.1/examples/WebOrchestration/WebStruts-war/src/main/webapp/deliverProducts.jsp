<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<head>
		<title>EJB Orchestration Example</title>
	</head>
	<body>
		<h1>Deliver Products</h1>
		<s:form action="productsDelivered">
			<table id="products">
				<tr>
					<th>Product</th>
					<th>Quantity</th>
					<th /> <!-- Error -->
				</tr>
				<s:iterator value="products" status="row">
					<tr>
						<s:hidden name="products[%{#row.index}].productId" value="%{productId}" />
						<s:hidden name="products[%{#row.index}].name" value="%{name}" />
						
						<td><s:property value="name"/></td>
						<td><s:textfield name="products[%{#row.index}].quantity" value="%{quantity}" /></td>
						<td><s:property value="error"/></td>
					</tr>
				</s:iterator>
			</table>
			<s:submit name="deliver" value="Deliver" />
			<s:submit name="cancel" value="Cancel" action="index" />
		</s:form>
	</body>
</html>