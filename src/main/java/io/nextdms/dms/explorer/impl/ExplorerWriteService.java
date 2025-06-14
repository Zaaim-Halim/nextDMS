package io.nextdms.dms.explorer.impl;

import static io.nextdms.dms.explorer.ExplorerUtils.setNodeProperty;

import io.nextdms.dms.explorer.IExplorerWriteService;
import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExplorerWriteService implements IExplorerWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerWriteService.class);

    /**
     * Add new node and add mandatory jcr:content child node if the node type is a file type
     */
    @Override
    public String addNewNode(
        Session session,
        String path,
        String newNodeName,
        String primaryNodeType,
        String[] mixinTypes,
        String jcrContentFileName,
        boolean cancel
    ) throws RepositoryException {
        // TODO Deprecated, because the standard uploading have use it
        if (cancel) {
            return "Removed files";
        }
        if (null == path || path.equals("") || null == primaryNodeType || primaryNodeType.equals("")) {
            throw new RepositoryException("New node not added.");
        }
        Node pathNode;
        try {
            Item item = null;
            item = session.getItem(path);

            if (!(item instanceof Node)) {
                return null;
            }
            pathNode = (Node) item;
            Node newNode = pathNode.addNode(newNodeName, primaryNodeType);

            if (mixinTypes != null && mixinTypes.length > 0) {
                for (String mixinName : mixinTypes) newNode.addMixin(mixinName);
            }
            session.save();
        } catch (Exception e) {
            LOG.error("Node not added. ", e);
            throw new RepositoryException("Node not added. " + e.getMessage());
        }

        return "New node successfully created.";
    }

    @Override
    public String addMixinType(Session session, String path, String mixinType) throws RepositoryException {
        if (null == path || path.equals("") || null == mixinType || mixinType.equals("")) {
            throw new RepositoryException("Mixin type not added. ");
        }
        Node pathNode;
        try {
            Item item = null;
            item = session.getItem(path);
            if (null == item || !(item instanceof Node)) {
                return null;
            }
            Node node = (Node) item;
            node.addMixin(mixinType);
            session.save();
        } catch (Exception e) {
            LOG.error("Mixin type not added. ", e);
            throw new RepositoryException("Mixin type not added.  " + e.getMessage());
        }
        return "Mixin type successfully added.";
    }

    @Override
    public String removeMixinType(Session session, String path, String mixinType) throws RepositoryException {
        if (null == path || path.equals("") || null == mixinType || mixinType.equals("")) {
            throw new RepositoryException("Mixin type not removed. ");
        }
        Node pathNode;
        try {
            Item item = null;
            item = session.getItem(path);
            if (null == item || !(item instanceof Node)) {
                return null;
            }
            Node node = (Node) item;
            node.removeMixin(mixinType);
            session.save();
        } catch (Exception e) {
            LOG.error("Mixin type not removed. ", e);
            throw new RepositoryException("Mixin type not removed. " + e.getMessage());
        }
        return "Mixin type successfully removed.";
    }

    @Override
    public String moveNode(Session session, String sourcePath, String destinationPath) throws RepositoryException {
        String sourceName;
        try {
            if (null == sourcePath || sourcePath.equals("") || null == destinationPath || destinationPath.equals("")) {
                throw new Exception("Node not moved.");
            }
            int lastIndexOfSlash = sourcePath.lastIndexOf('/');
            sourceName = sourcePath.substring(lastIndexOfSlash + 1, sourcePath.length());
            if (sourceName.indexOf('[') >= 0) {
                sourceName = sourceName.substring(0, sourceName.indexOf('['));
            }
            if (destinationPath.equals("/")) {
                session.move(sourcePath, destinationPath + sourceName);
            } else {
                session.move(sourcePath, destinationPath + "/" + sourceName);
            }
            session.save();
        } catch (Exception e) {
            LOG.error("Node Not Moved. ", e);
            throw new RepositoryException("Node Not Moved. " + e.getMessage());
        }

        return "Successfully moved. " + sourcePath + " to " + destinationPath;
    }

    @Override
    public String renameNode(Session session, String sourcePath, String newName) throws RepositoryException {
        String oldName;
        String newPath;
        try {
            if (null == sourcePath || sourcePath.equals("") || null == newName || newName.equals("")) {
                throw new Exception("Node not renamed.");
            }
            int lastIndexOfSlash = sourcePath.lastIndexOf('/');
            oldName = sourcePath.substring(lastIndexOfSlash + 1, sourcePath.length());
            newPath = sourcePath.substring(0, lastIndexOfSlash + 1) + newName;
            session.move(sourcePath, newPath);
            session.save();
        } catch (Exception e) {
            LOG.error("Node Not Renamed. ", e);
            throw new RepositoryException("Node Not Renamed. " + e.getMessage());
        }

        return "Successfully renamed from " + oldName + " to " + newName;
    }

    @Override
    public String moveNodes(Session session, Map<String, String> nodeMap) throws RepositoryException {
        if (nodeMap == null || nodeMap.isEmpty()) {
            throw new RepositoryException("No nodes specified for moving");
        }

        StringBuilder resultBuilder = new StringBuilder();
        int successCount = 0;
        int failCount = 0;

        try {
            for (Map.Entry<String, String> entry : nodeMap.entrySet()) {
                String sourcePath = entry.getKey();
                String destinationPath = entry.getValue();

                try {
                    if (sourcePath == null || sourcePath.isEmpty() || destinationPath == null || destinationPath.isEmpty()) {
                        failCount++;
                        continue;
                    }

                    int lastIndexOfSlash = sourcePath.lastIndexOf('/');
                    String sourceName = sourcePath.substring(lastIndexOfSlash + 1);
                    if (sourceName.indexOf('[') >= 0) {
                        sourceName = sourceName.substring(0, sourceName.indexOf('['));
                    }

                    String targetPath;
                    if (destinationPath.equals("/")) {
                        targetPath = destinationPath + sourceName;
                    } else {
                        targetPath = destinationPath + "/" + sourceName;
                    }

                    session.move(sourcePath, targetPath);
                    successCount++;
                } catch (Exception e) {
                    LOG.error("Failed to move node: " + sourcePath + " to " + destinationPath, e);
                    failCount++;
                }
            }

            session.save();
            resultBuilder.append("Successfully moved ").append(successCount).append(" nodes");
            if (failCount > 0) {
                resultBuilder.append(", failed to move ").append(failCount).append(" nodes");
            }
        } catch (Exception e) {
            LOG.error("Error during bulk move operation", e);
            throw new RepositoryException("Error during bulk move operation: " + e.getMessage());
        }

        return resultBuilder.toString();
    }

    @Override
    public String cutAndPasteNode(Session session, String sourcePath, String destinationPath) throws RepositoryException {
        try {
            if (null == sourcePath || sourcePath.equals("") || null == destinationPath || destinationPath.equals("")) {
                throw new Exception("Node not cut.");
            }
            copyNode(session, sourcePath, destinationPath);
            deleteNode(session, sourcePath);
        } catch (Exception e) {
            LOG.error("Node not cut. ", e);
            throw new RepositoryException("Node not cut. " + e.getMessage());
        }

        return "Successfully cut and pasted " + sourcePath + " to " + destinationPath;
    }

    @Override
    public String copyNode(Session session, String sourcePath, String destinationPath) throws RepositoryException {
        String sourceName;
        try {
            if (null == sourcePath || sourcePath.equals("") || null == destinationPath || destinationPath.equals("")) {
                throw new Exception("Node not copied.");
            }
            int lastIndexOfSlash = sourcePath.lastIndexOf('/');
            sourceName = sourcePath.substring(lastIndexOfSlash + 1, sourcePath.length());
            if (sourceName.indexOf('[') >= 0) {
                sourceName = sourceName.substring(0, sourceName.indexOf('['));
            }
            if (destinationPath.equals("/")) {
                session.getWorkspace().copy(sourcePath, destinationPath + sourceName);
            } else {
                session.getWorkspace().copy(sourcePath, destinationPath + "/" + sourceName);
            }
            session.save();
        } catch (Exception e) {
            LOG.error("Node not copied. ", e);
            throw new RepositoryException("Node not copied. " + e.getMessage());
        }

        return "Successfully copied " + sourcePath + " to " + destinationPath;
    }

    @Override
    public String copyNodes(Session session, Map<String, String> nodeMap) throws RepositoryException {
        if (nodeMap == null || nodeMap.isEmpty()) {
            throw new RepositoryException("No nodes specified for copying");
        }

        StringBuilder resultBuilder = new StringBuilder();
        int successCount = 0;
        int failCount = 0;

        try {
            for (Map.Entry<String, String> entry : nodeMap.entrySet()) {
                String sourcePath = entry.getKey();
                String destinationPath = entry.getValue();

                try {
                    if (sourcePath == null || sourcePath.isEmpty() || destinationPath == null || destinationPath.isEmpty()) {
                        failCount++;
                        continue;
                    }

                    int lastIndexOfSlash = sourcePath.lastIndexOf('/');
                    String sourceName = sourcePath.substring(lastIndexOfSlash + 1);
                    if (sourceName.indexOf('[') >= 0) {
                        sourceName = sourceName.substring(0, sourceName.indexOf('['));
                    }

                    String targetPath;
                    if (destinationPath.equals("/")) {
                        targetPath = destinationPath + sourceName;
                    } else {
                        targetPath = destinationPath + "/" + sourceName;
                    }

                    session.getWorkspace().copy(sourcePath, targetPath);
                    successCount++;
                } catch (Exception e) {
                    LOG.error("Failed to copy node: " + sourcePath + " to " + destinationPath, e);
                    failCount++;
                }
            }

            session.save();
            resultBuilder.append("Successfully copied ").append(successCount).append(" nodes");
            if (failCount > 0) {
                resultBuilder.append(", failed to copy ").append(failCount).append(" nodes");
            }
        } catch (Exception e) {
            LOG.error("Error during bulk copy operation", e);
            throw new RepositoryException("Error during bulk copy operation: " + e.getMessage());
        }

        return resultBuilder.toString();
    }

    @Override
    public String deleteNode(Session session, String sourcePath) throws RepositoryException {
        if (null == sourcePath || sourcePath.equals("")) {
            throw new RepositoryException("Node source missing");
        }
        try {
            Item item = session.getItem(sourcePath);
            item.remove();
            session.save();
        } catch (Exception e) {
            LOG.error("Node not deleted. ", e);
            throw new RepositoryException("Node not deleted. " + e.getMessage());
        }

        return "Successfully deleted. " + sourcePath;
    }

    //not used
    @Override
    public String saveNodeDetails(Session session, String sourcePath, JcrNode jcrNode) throws RepositoryException {
        return "";
    }

    // Properties
    @Override
    public String addNewProperty(Session session, String sourcePath, String name, JcrProperty value) throws RepositoryException {
        if (null == sourcePath || sourcePath.equals("")) {
            throw new RepositoryException("Property not added.");
        }
        try {
            Item item = session.getItem(sourcePath);
            if (!(item instanceof Node)) {
                return null;
            }
            Node pathNode = (Node) item;
            setNodeProperty(pathNode, name, value);

            session.save();
        } catch (Exception e) {
            LOG.error("Property not added. ", e);
            throw new RepositoryException("Property not added. " + e.getMessage());
        }

        return "Successfully added new property at " + sourcePath;
    }

    @Override
    public String deleteProperty(Session session, String sourcePath, String name) throws RepositoryException {
        if (null == sourcePath || sourcePath.equals("") || null == name || name.equals("")) {
            throw new RepositoryException("Property not deleted.");
        }
        try {
            Item item = session.getItem(sourcePath);
            if (!(item instanceof Node)) {
                return null;
            }
            Node pathNode = (Node) item;
            pathNode.getProperty(name).remove();
            session.save();
        } catch (Exception e) {
            LOG.error("Property not deleted. ", e);
            throw new RepositoryException("Property not deleted. " + e.getMessage());
        }

        return "Successfully deleted " + name + " property at " + sourcePath;
    }

    @Override
    public String saveProperties(Session session, String sourcePath, JcrNode jcrNode) throws RepositoryException {
        if (null == jcrNode) {
            throw new RepositoryException("Properties not saved.");
        }
        try {
            Item item = session.getItem(sourcePath);
            if (!(item instanceof Node)) {
                return null;
            }

            Node pathNode = (Node) item;
            for (Iterator<Map.Entry<String, JcrProperty>> iterator = jcrNode.properties().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, JcrProperty> propertyPair = iterator.next();
                setNodeProperty(pathNode, propertyPair.getValue().name(), propertyPair.getValue());
            }
            session.save();
        } catch (Exception e) {
            LOG.error("Properties not saved. ", e);
            throw new RepositoryException("Properties not saved. " + e.getMessage());
        }

        return "Successfully saved. " + sourcePath;
    }

    @Override
    public String saveProperty(Session session, String sourcePath, String property, JcrProperty value) throws RepositoryException {
        if (null == sourcePath || null == property || null == value) {
            throw new RepositoryException("Property not saved.");
        }
        try {
            Item item = session.getItem(sourcePath);
            if (null == item) {
                return null;
            }
            Node pathNode = (Node) item;
            setNodeProperty(pathNode, property, value);
            session.save();
        } catch (Exception e) {
            LOG.error("Property value not saved. ", e);
            throw new RepositoryException("Property not saved. " + e.getMessage());
        }

        return "Successfully saved property " + sourcePath;
    }

    public String savePropertyBinaryValue(Session session, String sourcePath, String property, InputStream inputStream)
        throws RepositoryException {
        if (null == sourcePath || null == property || null == inputStream) {
            throw new RepositoryException("Property not saved.");
        }
        try {
            Item item = session.getItem(sourcePath);
            if (null == item) {
                return null;
            }
            Node pathNode = (Node) item;
            pathNode.setProperty(property, session.getValueFactory().createBinary(inputStream));
            session.save();
        } catch (Exception e) {
            LOG.error("Binary Property not saved. ", e);
            throw new RepositoryException("Property not saved. " + e.getMessage());
        }

        return "Successfully saved. " + sourcePath;
    }

    @Override
    public Boolean addNodeTypes(Session session, String cnd) throws RepositoryException {
        if (cnd == null || cnd.isEmpty()) {
            throw new RepositoryException("CND content is missing");
        }

        try {
            // Use Jackrabbit's CndImporter to register node types from the CND string
            NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
            CndImporter.registerNodeTypes(new StringReader(cnd), session);
            session.save();
            LOG.info("Successfully registered node types from CND");
            return true;
        } catch (ParseException e) {
            LOG.error("Failed to parse CND content", e);
            throw new RepositoryException("Failed to parse CND content: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to register node types", e);
            throw new RepositoryException("Failed to register node types: " + e.getMessage());
        }
    }

    @Override
    public Boolean changeNodeTypeIconAssociation(Session session, String nodeType, String iconPath) throws RepositoryException {
        if (nodeType == null || nodeType.isEmpty() || iconPath == null || iconPath.isEmpty()) {
            throw new RepositoryException("Node type or icon path is missing");
        }

        try {
            // Check if the node type exists
            if (!session.getWorkspace().getNodeTypeManager().hasNodeType(nodeType)) {
                throw new RepositoryException("Node type does not exist: " + nodeType);
            }

            // Store the icon association in a system node
            // Assuming there's a specific location where icon associations are stored
            String iconAssociationPath = "/system/nodetype-icons";

            // Check if the icon association path exists, create if it doesn't
            if (!session.nodeExists(iconAssociationPath)) {
                Node rootNode = session.getRootNode();
                Node systemNode;
                if (!rootNode.hasNode("system")) {
                    systemNode = rootNode.addNode("system", "nt:folder");
                } else {
                    systemNode = rootNode.getNode("system");
                }
                systemNode.addNode("nodetype-icons", "nt:unstructured");
            }

            Node iconAssociationNode = session.getNode(iconAssociationPath);
            iconAssociationNode.setProperty(nodeType, iconPath);

            session.save();
            LOG.info("Successfully associated icon with node type: " + nodeType + " -> " + iconPath);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to associate icon with node type", e);
            throw new RepositoryException("Failed to associate icon with node type: " + e.getMessage());
        }
    }
}
