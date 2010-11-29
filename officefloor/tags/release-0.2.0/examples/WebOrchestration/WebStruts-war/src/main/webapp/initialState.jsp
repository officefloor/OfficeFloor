<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
	<body>
		<h1>Initial State</h1>
		<h2 id="name"><s:property value="customerName" /></h2>
		<h2 id="email"><s:property value="customerEmail" /></h2>
		<s:iterator value="products">
			<h2 id="product-<s:property value='name'/>"><s:property value='productId'/></h2>
        </s:iterator>
	</body>
</html>