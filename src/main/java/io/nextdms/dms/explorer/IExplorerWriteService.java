package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.io.InputStream;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface IExplorerWriteService {
    String addNewNode(
        Session session,
        String path,
        String newNodeName,
        String primaryNodeType,
        String[] mixinTypes,
        String jcrContentFileName,
        boolean cancel
    ) throws RepositoryException;
    String addMixinType(Session session, String path, String mixinType) throws RepositoryException;
    String removeMixinType(Session session, String path, String mixinType) throws RepositoryException;
    String moveNode(Session session, String sourcePath, String destinationPath) throws RepositoryException;
    String renameNode(Session session, String sourcePath, String newName) throws RepositoryException;
    String copyNode(Session session, String sourcePath, String destinationPath) throws RepositoryException;
    String cutAndPasteNode(Session session, String sourcePath, String destinationPath) throws RepositoryException;
    String moveNodes(Session session, Map<String, String> nodeMap) throws RepositoryException;
    String copyNodes(Session session, Map<String, String> nodeMap) throws RepositoryException;
    String deleteNode(Session session, String sourcePath) throws RepositoryException;
    String saveNodeDetails(Session session, String sourcePath, JcrNode jcrNode) throws RepositoryException;
    String addNewProperty(Session session, String sourcePath, String name, JcrProperty value) throws RepositoryException;
    String deleteProperty(Session session, String sourcePath, String name) throws RepositoryException;
    String saveProperties(Session session, String sourcePath, JcrNode jcrNode) throws RepositoryException;
    String saveProperty(Session session, String sourcePath, String property, JcrProperty value) throws RepositoryException;
    String savePropertyBinaryValue(Session session, String sourcePath, String property, InputStream value) throws RepositoryException;
    Boolean addNodeTypes(Session session, String cnd) throws RepositoryException;
    Boolean changeNodeTypeIconAssociation(Session session, String nodeType, String iconPath) throws RepositoryException;
}
