package io.nextdms.dms.explorer.impl;

import io.nextdms.dms.explorer.ExplorerUtils;
import io.nextdms.dms.explorer.IExplorerReadService;
import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class ExplorerReadService implements IExplorerReadService {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerReadService.class);

    @Override
    public List<Map<String, List<JcrNode>>> getNodeTree(Session session, String path) throws RepositoryException {
        List<Map<String, List<JcrNode>>> returnList;
        try {
            if (null == path || path.equals("")) {
                path = "/";
            }
            String[] pathSplit = path.split("/");
            returnList = new ArrayList<>();
            Map<String, List<JcrNode>> treeAssociationMap = null;
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < pathSplit.length; i++) {
                treeAssociationMap = new HashMap<>();
                pathBuilder.append(pathSplit[i].trim()).append("/");
                treeAssociationMap.put(pathBuilder.toString(), getNode(session, pathBuilder.toString()));
                returnList.add(treeAssociationMap);
            }
        } catch (Exception e) {
            LOG.error("Failed fetching Node Tree. ", e);
            throw new RepositoryException(e.getMessage());
        }

        return returnList;
    }

    @Override
    public List<JcrNode> getNode(Session session, String path) throws RepositoryException {
        Node node = session.getNode(path);
        return ExplorerUtils.getChildreen(node);
    }

    @Override
    public List<String> getAvailableNodeTypes(Session session) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator nodeTypes = nodeTypeManager.getAllNodeTypes();
        List<String> nodeTypeNames = new ArrayList<>();
        while (nodeTypes.hasNext()) {
            NodeType nodeType = nodeTypes.nextNodeType();
            nodeTypeNames.add(nodeType.getName());
        }
        return nodeTypeNames;
    }

    @Override
    public List<String> getMixinNodeTypes(Session session) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator nodeTypes = nodeTypeManager.getMixinNodeTypes();
        List<String> nodeTypeNames = new ArrayList<>();
        while (nodeTypes.hasNext()) {
            NodeType nodeType = nodeTypes.nextNodeType();
            nodeTypeNames.add(nodeType.getName());
        }
        return nodeTypeNames;
    }

    @Override
    public Page<JcrNode> fullTextSearch(Session session, String query, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = this.createQuery(queryManager, query, Query.JCR_SQL2, pageable);
        return this.transformToPage(
                this.getSearcResult(session, result),
                pageable,
                getSearchTotalCount(queryManager, pageable, Query.JCR_SQL2, query)
            );
    }

    @Override
    public Page<JcrNode> xpathSearch(Session session, String query, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = this.createQuery(queryManager, query, Query.XPATH, pageable);
        return this.transformToPage(
                this.getSearcResult(session, result),
                pageable,
                getSearchTotalCount(queryManager, pageable, Query.XPATH, query)
            );
    }

    @Override
    public Page<JcrNode> sqlSearch(Session session, String query, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = this.createQuery(queryManager, query, Query.JCR_SQL2, pageable);
        return this.transformToPage(
                this.getSearcResult(session, result),
                pageable,
                getSearchTotalCount(queryManager, pageable, Query.JCR_SQL2, query)
            );
    }

    @Override
    public List<Map<String, String>> getNodeTypeIcons(Session session) throws RepositoryException {
        return List.of();
    }

    @Override
    public String getBrowsableContentFilterRegex(Session session) throws RepositoryException {
        return "";
    }

    @Override
    public Map<String, JcrProperty> getProperties(Session session, Node node) throws RepositoryException {
        return ExplorerUtils.getProperties(node);
    }

    private QueryResult createQuery(QueryManager queryManager, String queryStr, String queryType, Pageable pageable)
        throws RepositoryException {
        Query query = queryManager.createQuery(queryStr, queryType);
        if (pageable != null) {
            query.setLimit(pageable.getPageSize());
            query.setOffset(pageable.getOffset());
        }
        if (pageable != null) {
            query.setLimit(pageable.getPageSize());
            query.setOffset(pageable.getOffset());
        }
        return query.execute();
    }

    private List<JcrNode> getSearcResult(Session session, QueryResult queryResult) throws RepositoryException {
        List<JcrNode> nodesList = new ArrayList<>();
        NodeIterator nodes = queryResult.getNodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            JcrNode jcrNode = new JcrNode(
                node.getIdentifier(),
                node.getName(),
                node.getPath(),
                node.getPrimaryNodeType().getName(),
                Stream.of(node.getMixinNodeTypes()).map(NodeType::getName).toList(),
                getProperties(session, node)
            );
            nodesList.add(jcrNode);
        }
        return nodesList;
    }

    private <T> Page<T> transformToPage(List<T> content, Pageable pageable, int total) {
        if (pageable != null) {
            return new PageImpl<>(content, pageable, total);
        } else {
            return new PageImpl<>(content);
        }
    }

    private int getSearchTotalCount(QueryManager queryManager, Pageable pageable, String queryType, String query)
        throws RepositoryException {
        if (pageable == null) {
            return 0;
        } else {
            return switch (queryType) {
                case Query.JCR_SQL2:
                    Query sqlQuery = queryManager.createQuery(this.transformToCountQuery(query), queryType);
                    QueryResult sqlResult = sqlQuery.execute();
                    yield (int) sqlResult.getNodes().getSize(); // we might need to change this to a more efficient way . and avoid trunckating
                case Query.XPATH:
                    Query xpathQuery = queryManager.createQuery(String.format("COUNT(%s)", query), queryType);
                    QueryResult xpathResult = xpathQuery.execute();
                    yield (int) xpathResult.getNodes().getSize();
                default:
                    yield 0;
            };
        }
    }

    private String transformToCountQuery(String query) {
        // Regular expression pattern to match 'SELECT' (case-insensitive),
        // followed by any characters, and then 'FROM' (case-insensitive).
        String regex = "(?i)SELECT\\s+.*?\\s+FROM";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);
        // Replace the part between SELECT and FROM with newSelection
        return matcher.replaceFirst("SELECT COUNT(*) FROM");
    }
}
