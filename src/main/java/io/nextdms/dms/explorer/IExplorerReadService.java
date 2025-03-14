package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IExplorerReadService {
    List<Map<String, List<JcrNode>>> getNodeTree(Session session, String path) throws RepositoryException;
    List<JcrNode> getNode(Session session, String path, String UUID) throws RepositoryException;
    Map<String, JcrProperty> getProperties(Session session, Node node) throws RepositoryException;
    Map<String, JcrProperty> getProperties(Session session, String path, String uuid) throws RepositoryException;
    List<String> getAvailableNodeTypes(Session session) throws RepositoryException;
    List<String> getMixinNodeTypes(Session session) throws RepositoryException;
    Page<JcrNode> fullTextSearch(Session session, String query, Pageable pageable) throws RepositoryException;
    Page<JcrNode> xpathSearch(Session session, String query, Pageable pageable) throws RepositoryException;
    Page<JcrNode> sqlSearch(Session session, String query, Pageable pageable) throws RepositoryException;
    List<Map<String, String>> getNodeTypeIcons(Session session) throws RepositoryException;
    String getBrowsableContentFilterRegex(Session session) throws RepositoryException;
}
