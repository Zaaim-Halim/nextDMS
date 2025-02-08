package io.nextdms.dms.config;

import static org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder.newMongoDocumentNodeStoreBuilder;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.*;
import javax.jcr.Repository;
import javax.sql.DataSource;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoBlobStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBBlobStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OakConfiguration {

    @Bean
    @Profile({ "mongo" })
    public MongoDatabase mongoDatabase() {
        return mongoMongoClient().getDatabase("oak");
    }

    //TODO: change the connection string to use the environment variable
    //TODO: move to BlobStoreProvider
    @Bean
    @Profile({ "mongo" })
    public MongoClient mongoMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
            .applyToConnectionPoolSettings(builder -> builder.maxSize(100))
            .build();
        return MongoClients.create(settings);
    }

    @Bean
    @Profile({ "rdb" })
    public BlobStore blobStore(DataSource dataSource) {
        return new RDBBlobStore(dataSource);
    }

    @Bean
    @Profile({ "rdb" })
    public NodeStore nodeStore(DataSource dataSource) {
        return new RDBDocumentNodeStoreBuilder()
            //.setClusterId(-1)  // disable clustering
            .setRDBConnection(dataSource)
            .setBlobStore(blobStore(dataSource))
            .setExecutor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    }

    @Bean
    @Profile({ "mongo" })
    public BlobStore blobStoreMongo() {
        return new MongoBlobStore(mongoDatabase());
    }

    //TODO: change the connection string to use the environment variable
    @Bean
    @Profile({ "mongo" })
    public NodeStore nodeStoreMongo(DataSource dataSource) {
        return newMongoDocumentNodeStoreBuilder()
            //.setClusterId(-1)  // disable clustering
            .setMongoDB("mongodb://localhost:27017", "oak", 100)
            .setBlobStore(blobStoreMongo())
            .setExecutor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    }

    @Bean
    public Repository repository(DataSource dataSource) {
        return new Jcr(new Oak(nodeStore(dataSource)), true).with(new OakRepositoryInitializer()).createRepository();
    }
}
