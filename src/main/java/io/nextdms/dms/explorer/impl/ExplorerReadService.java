package io.nextdms.dms.explorer.impl;

import static io.nextdms.dms.explorer.ExplorerUtils.*;

import io.nextdms.dms.explorer.ExplorerUtils;
import io.nextdms.dms.explorer.IExplorerReadService;
import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.util.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
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
                treeAssociationMap.put(pathBuilder.toString(), getNode(session, pathBuilder.toString(), null));
                returnList.add(treeAssociationMap);
            }
        } catch (Exception e) {
            LOG.error("Failed fetching Node Tree. ", e);
            throw new RepositoryException(e.getMessage());
        }

        return returnList;
    }

    @Override
    public List<JcrNode> getNode(Session session, String path, String UUID) throws RepositoryException {
        Node node = StringUtils.hasText(UUID) ? session.getNodeByIdentifier(UUID) : session.getNode(path);
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
        QueryResult result = createQuery(queryManager, query, Query.JCR_SQL2, pageable);
        return transformToPage(
            getSearcResult(session, result),
            pageable,
            getSearchTotalCount(queryManager, pageable, Query.JCR_SQL2, query)
        );
    }

    @Override
    public Page<JcrNode> xpathSearch(Session session, String query, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = createQuery(queryManager, query, Query.XPATH, pageable);
        return transformToPage(getSearcResult(session, result), pageable, getSearchTotalCount(queryManager, pageable, Query.XPATH, query));
    }

    @Override
    public Page<JcrNode> sqlSearch(Session session, String query, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = createQuery(queryManager, query, Query.JCR_SQL2, pageable);
        return transformToPage(
            getSearcResult(session, result),
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

    @Override
    public Map<String, JcrProperty> getProperties(Session session, String path, String uuid) throws RepositoryException {
        final var node = StringUtils.hasText(uuid) ? session.getNodeByIdentifier(uuid) : session.getNode(path);
        return ExplorerUtils.getProperties(node);
    }
}
