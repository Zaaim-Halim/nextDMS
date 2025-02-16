package io.nextdms.dms.config;

import java.util.concurrent.*;
import javax.jcr.Repository;
import javax.sql.DataSource;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBBlobStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.plugins.index.WhiteboardIndexEditorProvider;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.commit.WhiteboardEditorProvider;
import org.apache.jackrabbit.oak.spi.query.WhiteboardIndexProvider;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OakConfiguration {

    @Bean
    @Profile({ "prod", "dev", "rdb" })
    public BlobStore blobStore(DataSource dataSource) {
        return new RDBBlobStore(dataSource);
    }

    @Bean
    @Profile({ "prod", "dev", "rdb" })
    public NodeStore nodeStore(DataSource dataSource) {
        return new RDBDocumentNodeStoreBuilder()
            //.setClusterId(-1)  // disable clustering
            .setRDBConnection(dataSource)
            .setBlobStore(blobStore(dataSource))
            .setExecutor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    }

    /*
  sling immplementation
   https://github.com/apache/sling-org-apache-sling-jcr-oak-server/blob/master/src/main/java/org/apache/sling/jcr/oak/server/internal/OakSlingRepositoryManager.java

 */
    @Bean
    public Repository repository(DataSource dataSource) {
        try {
            return new Jcr(new Oak(nodeStore(dataSource)).withAsyncIndexing("async", 5), false)
                .with(new OakRepositoryInitializer())
                .with(new WhiteboardEditorProvider())
                .with(new WhiteboardIndexProvider())
                .with(new WhiteboardIndexEditorProvider())
                .withObservationQueueLength(1000)
                .withFastQueryResultSize(false)
                .createRepository();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }
}
