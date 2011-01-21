// OfficeFloor source implementation to simplify creating a HTTP Server
HttpServerAutoWireOfficeFloorSource server = new HttpServerAutoWireOfficeFloorSource();
        
// Add dynamic web page
server.addHttpTemplate("example.ofp", Example.class, "example");
        
// Add configured DataSource for dependency injection
AutoWireObject object = server.addManagedObject(DataSourceManagedObjectSource.class, null, DataSource.class);
object.addProperty("data.source.class.name", "org.hsqldb.jdbc.jdbcDataSource");
object.addProperty("Database", "jdbc:hsqldb:mem:exampledb");
        
// Assign Team (specific thread pool) responsible for executing tasks with a DataSource dependency
server.assignTeam(LeaderFollowerTeam.class, DataSource.class);
        
// Start the HTTP Server
server.openOfficeFloor();