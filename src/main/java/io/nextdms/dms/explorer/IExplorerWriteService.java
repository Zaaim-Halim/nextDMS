package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.io.InputStream;
import java.util.Map;
import javax.jcr.RepositoryException;

public interface IExplorerWriteService {
    String addNewNode(
        String path,
        String newNodeName,
        String primaryNodeType,
        String[] mixinTypes,
        String jcrContentFileName,
        boolean cancel
    ) throws RepositoryException;
    String addMixinType(String path, String mixinType) throws RepositoryException;
    String removeMixinType(String path, String mixinType) throws RepositoryException;
    String moveNode(String sourcePath, String destinationPath) throws RepositoryException;
    String renameNode(String sourcePath, String newName) throws RepositoryException;
    String copyNode(String sourcePath, String destinationPath) throws RepositoryException;
    String cutAndPasteNode(String sourcePath, String destinationPath) throws RepositoryException;
    String moveNodes(Map<String, String> nodeMap) throws RepositoryException;
    String copyNodes(Map<String, String> nodeMap) throws RepositoryException;
    String deleteNode(String sourcePath) throws RepositoryException;
    String saveNodeDetails(String sourcePath, JcrNode jcrNode) throws RepositoryException;
    String addNewProperty(String sourcePath, String name, JcrProperty value) throws RepositoryException;
    String deleteProperty(String sourcePath, String name) throws RepositoryException;
    String saveProperties(String sourcePath, JcrNode jcrNode) throws RepositoryException;
    String saveProperty(String sourcePath, String property, JcrProperty value) throws RepositoryException;
    String savePropertyBinaryValue(String sourcePath, String property, InputStream value) throws RepositoryException;
    Boolean addNodeTypes(String cnd) throws RepositoryException;
    Boolean changeNodeTypeIconAssociation(String nodeType, String iconPath) throws RepositoryException;
}
