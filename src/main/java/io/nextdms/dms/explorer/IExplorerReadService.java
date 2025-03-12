package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.springframework.stereotype.Service;

@Service
public interface IExplorerReadService {
    List<Map<String, List<JcrNode>>> getNodeTree(Session session, String path) throws RepositoryException;
    List<JcrNode> getNode(Session session, String path) throws RepositoryException;
    Map<String, JcrProperty> getProperties(Session session, Node node) throws RepositoryException;
    List<String> getAvailableNodeTypes(Session session) throws RepositoryException;
    List<String> getMixinNodeTypes(Session session) throws RepositoryException;
    List<JcrNode> fullTextSearch(Session session, String query) throws RepositoryException;
    List<JcrNode> xpathSearch(Session session, String query) throws RepositoryException;
    List<JcrNode> sqlSearch(Session session, String query) throws RepositoryException;
    List<Map<String, String>> getNodeTypeIcons(Session session) throws RepositoryException;
    String getBrowsableContentFilterRegex(Session session) throws RepositoryException;
}
