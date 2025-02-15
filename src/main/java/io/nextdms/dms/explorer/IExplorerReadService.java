package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface IExplorerReadService {
    List<Map<String, List<JcrNode>>> getNodeTree(String path) throws RepositoryException;
    List<JcrNode> getNode(String path) throws RepositoryException;
    Map<String, JcrProperty> getProperties(Node node) throws RepositoryException;
    List<String> getAvailableNodeTypes() throws RepositoryException;
    List<String> getMixinNodeTypes() throws RepositoryException;
    List<JcrNode> fullTextSearch(String query) throws RepositoryException;
    List<JcrNode> xpathSearch(String query) throws RepositoryException;
    List<JcrNode> sqlSearch(String query) throws RepositoryException;
    List<Map<String, String>> getNodeTypeIcons() throws RepositoryException;
    String getBrowsableContentFilterRegex() throws RepositoryException;
}
