// OfficeFloor source implementation to simplify creating a HTTP Server
HttpServerOfficeFloorSource server = new HttpServerOfficeFloorSource();
        
// Add dynamic web page
server.addHttpTemplate("example.ofp", Example.class, "example");
        
// Add Object for dependency injection
PropertyList properties = server.addObject(DataSource.class, DataSourceManagedObjectSource.class, null);
// Configure DataSource, e.g. properties.addProperty("jdbcDriver").setValue("jdbcDriver");
        
// Assign Team (specific thread pool) responsible for executing tasks with a DataSource dependency
server.assignTeam(LeaderFollowerTeam.class, DataSource.class);
        
// Start the HTTP Server
server.openOfficeFloor();