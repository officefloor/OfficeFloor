const { default: cosmosServer } = require("@zeit/cosmosdb-server");

cosmosServer().listen(3000, () => {
  console.log(`Cosmos DB server running at https://localhost:3000`);
});
