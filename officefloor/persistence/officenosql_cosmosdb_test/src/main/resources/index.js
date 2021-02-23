const { default: cosmosServer } = require("@zeit/cosmosdb-server");

var port = ${PORT}

cosmosServer().listen(port, () => {
  console.log(`Cosmos DB server running at https://localhost:${port}`);
});
