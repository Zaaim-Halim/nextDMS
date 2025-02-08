package io.nextdms.dms.config;

import static org.apache.jackrabbit.JcrConstants.*;
import static org.apache.jackrabbit.oak.api.Type.NAME;
import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.INDEX_DEFINITIONS_NODE_TYPE;
import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.TYPE_PROPERTY_NAME;
import static org.apache.jackrabbit.oak.plugins.memory.ModifiedNodeState.squeeze;
import static org.apache.jackrabbit.oak.spi.nodetype.NodeTypeConstants.*;
import static org.apache.jackrabbit.oak.spi.version.VersionConstants.REP_VERSIONSTORAGE;
import static org.apache.jackrabbit.oak.spi.version.VersionConstants.VERSION_STORE_INIT;

import com.sun.istack.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.jackrabbit.oak.InitialContent;
import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.apache.jackrabbit.oak.plugins.index.IndexUtils;
import org.apache.jackrabbit.oak.plugins.index.counter.NodeCounterEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexConstants;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.apache.jackrabbit.oak.plugins.name.Namespaces;
import org.apache.jackrabbit.oak.plugins.nodetype.write.NodeTypeRegistry;
import org.apache.jackrabbit.oak.plugins.tree.factories.RootFactory;
import org.apache.jackrabbit.oak.spi.lifecycle.RepositoryInitializer;
import org.apache.jackrabbit.oak.spi.state.ApplyDiff;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.spi.version.VersionConstants;

public class OakRepositoryInitializer implements RepositoryInitializer {

    /**
     * Whether to pre-populate the version store with intermediate nodes.
     */
    private boolean prePopulateVS = false;

    @Override
    public void initialize(@NotNull NodeBuilder builder) {
        builder.setProperty(JCR_PRIMARYTYPE, NT_REP_ROOT, Type.NAME);

        if (!builder.hasChildNode(JCR_SYSTEM)) {
            NodeBuilder system = builder.child(JCR_SYSTEM);
            system.setProperty(JCR_PRIMARYTYPE, NT_REP_SYSTEM, Type.NAME);

            system.child(JCR_VERSIONSTORAGE).setProperty(JCR_PRIMARYTYPE, REP_VERSIONSTORAGE, Type.NAME);
            system.child(JCR_NODE_TYPES).setProperty(JCR_PRIMARYTYPE, NT_REP_NODE_TYPES, Type.NAME);
            system.child(VersionConstants.JCR_ACTIVITIES).setProperty(JCR_PRIMARYTYPE, VersionConstants.REP_ACTIVITIES, Type.NAME);

            Namespaces.setupNamespaces(system);
        }

        NodeBuilder versionStorage = builder.child(JCR_SYSTEM).child(JCR_VERSIONSTORAGE);
        if (prePopulateVS && !isInitialized(versionStorage)) {
            createIntermediateNodes(versionStorage);
        }

        //--------------------------------- < Index > ---------------------

        if (!builder.hasChildNode(IndexConstants.INDEX_DEFINITIONS_NAME)) {
            NodeBuilder index = IndexUtils.getOrCreateOakIndex(builder);

            NodeBuilder uuid = IndexUtils.createIndexDefinition(index, "uuid", true, true, List.of(JCR_UUID), null);
            uuid.setProperty("info", "Oak index for UUID lookup (direct lookup of nodes with the mixin 'mix:referenceable').");
            NodeBuilder nodetype = IndexUtils.createIndexDefinition(
                index,
                "nodetype",
                true,
                false,
                List.of(JCR_PRIMARYTYPE, JCR_MIXINTYPES),
                null
            );
            nodetype.setProperty(
                "info",
                "Oak index for queries with node type, and possibly path restrictions, " +
                "for example \"/jcr:root/content//element(*, mix:language)\"."
            );
            IndexUtils.createReferenceIndex(index);

            index
                .child("counter")
                .setProperty(JCR_PRIMARYTYPE, INDEX_DEFINITIONS_NODE_TYPE, NAME)
                .setProperty(TYPE_PROPERTY_NAME, NodeCounterEditorProvider.TYPE)
                .setProperty(IndexConstants.ASYNC_PROPERTY_NAME, IndexConstants.ASYNC_PROPERTY_NAME)
                .setProperty(
                    "info",
                    "Oak index that allows to estimate " +
                    "how many nodes are stored below a given path, " +
                    "to decide whether traversing or using an index is faster."
                );

            // lucen index for full text search

            if (!index.hasChildNode("lucene")) {
                NodeBuilder lucenindex = index.child("lucene");

                lucenindex.setProperty(IndexConstants.TYPE_PROPERTY_NAME, LuceneIndexConstants.TYPE_LUCENE);
                lucenindex.setProperty(IndexConstants.REINDEX_PROPERTY_NAME, true);
                lucenindex.setProperty(IndexConstants.ASYNC_PROPERTY_NAME, "async");

                NodeBuilder indexRules = index.child(LuceneIndexConstants.INDEX_RULES);
                NodeBuilder ntBase = indexRules.child("nt:base");
                ntBase.setProperty(LuceneIndexConstants.INDEX_NODE_NAME, "nt:base");

                NodeBuilder properties = ntBase.child(LuceneIndexConstants.PROP_NODE);
                NodeBuilder allProps = properties.child("allProps");
                allProps.setProperty("propertyName", "propertyName");
                allProps.setProperty(LuceneIndexConstants.PROP_IS_REGEX, true);
                allProps.setProperty(LuceneIndexConstants.PROP_NODE_SCOPE_INDEX, true);
                allProps.setProperty(LuceneIndexConstants.PROP_ANALYZED, true);
                allProps.setProperty(LuceneIndexConstants.PROP_USE_IN_EXCERPT, true);
                allProps.setProperty(LuceneIndexConstants.PROP_PROPERTY_INDEX, true);
                lucenindex.setProperty("info", "Oak Lucen index that  that enables full-text search.");
            }
        }

        // squeeze node state before it is passed to store (OAK-2411)
        NodeState base = squeeze(builder.getNodeState());
        NodeStore store = new MemoryNodeStore(base);
        registerBuiltIn(RootFactory.createSystemRoot(store, null, null, null, null));
        NodeState target = store.getRoot();
        target.compareAgainstBaseState(base, new ApplyDiff(builder));
    }

    //--------------------------< internal >------------------------------------

    private static boolean isInitialized(NodeBuilder versionStorage) {
        PropertyState init = versionStorage.getProperty(VERSION_STORE_INIT);
        return init != null && init.getValue(Type.LONG) > 0;
    }

    private static void createIntermediateNodes(NodeBuilder versionStorage) {
        String fmt = "%02x";
        versionStorage.setProperty(VERSION_STORE_INIT, 1);
        for (int i = 0; i < 0xff; i++) {
            NodeBuilder c = storageChild(versionStorage, String.format(fmt, i));
            for (int j = 0; j < 0xff; j++) {
                storageChild(c, String.format(fmt, j));
            }
        }
    }

    private static NodeBuilder storageChild(NodeBuilder node, String name) {
        NodeBuilder c = node.child(name);
        if (!c.hasProperty(JCR_PRIMARYTYPE)) {
            c.setProperty(JCR_PRIMARYTYPE, REP_VERSIONSTORAGE, Type.NAME);
        }
        return c;
    }

    /**
     * Registers built in node types using the given {@link Root}.
     *
     * @param root the {@link Root} instance.
     */
    private static void registerBuiltIn(final Root root) {
        try {
            InputStream stream = InitialContent.class.getResourceAsStream("builtin_nodetypes.cnd");
            try {
                NodeTypeRegistry.register(root, stream, "built-in node types");
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read built-in node types", e);
        }
    }
}
