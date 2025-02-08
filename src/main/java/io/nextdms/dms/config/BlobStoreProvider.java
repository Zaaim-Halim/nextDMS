package io.nextdms.dms.config;

public class BlobStoreProvider {
    /* @Bean
    @Profile({ "mongo" })
    public BlobStore blobStoreMongo() {
        return new MongoBlobStore(mongoDatabase());
    }*/

    //TODO: change the connection string to use the environment variable
    /*@Bean
    @Profile({ "mongo" })
    public NodeStore nodeStoreMongo(DataSource dataSource) {
        return newMongoDocumentNodeStoreBuilder()
            //.setClusterId(-1)  // disable clustering
            .setMongoDB("mongodb://localhost:27017", "oak", 100)
            .setBlobStore(blobStoreMongo())
            .setExecutor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    }*/
    /*@Bean
    @Profile({ "mongo" })
    public MongoClient mongoMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
            .applyToConnectionPoolSettings(builder -> builder.maxSize(100))
            .build();
        return MongoClients.create(settings);
    }
     */

    /*@Bean
    @Profile({ "mongo" })
    public MongoDatabase mongoDatabase() {
        return mongoMongoClient().getDatabase("oak");
    }*/
}
