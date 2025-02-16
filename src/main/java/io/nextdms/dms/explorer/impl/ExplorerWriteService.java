package io.nextdms.dms.explorer.impl;

import static io.nextdms.dms.explorer.ExplorerUtils.setNodeProperty;

import io.nextdms.dms.explorer.IExplorerWriteService;
import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorerWriteService implements IExplorerWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerWriteService.class);
    private final Session session;

    public ExplorerWriteService(Session session) {
        this.session = session;
    }

    /**
     * Add new node and add mandatory jcr:content child node if the node type is a file type
     */
    @Override
    public String addNewNode(
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
    public String addMixinType(String path, String mixinType) throws RepositoryException {
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
    public String removeMixinType(String path, String mixinType) throws RepositoryException {
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
    public String moveNode(String sourcePath, String destinationPath) throws RepositoryException {
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
    public String renameNode(String sourcePath, String newName) throws RepositoryException {
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
    public String moveNodes(Map<String, String> nodeMap) throws RepositoryException {
        return null;
    }

    @Override
    public String cutAndPasteNode(String sourcePath, String destinationPath) throws RepositoryException {
        try {
            if (null == sourcePath || sourcePath.equals("") || null == destinationPath || destinationPath.equals("")) {
                throw new Exception("Node not cut.");
            }
            copyNode(sourcePath, destinationPath);
            deleteNode(sourcePath);
        } catch (Exception e) {
            LOG.error("Node not cut. ", e);
            throw new RepositoryException("Node not cut. " + e.getMessage());
        }

        return "Successfully cut and pasted " + sourcePath + " to " + destinationPath;
    }

    @Override
    public String copyNode(String sourcePath, String destinationPath) throws RepositoryException {
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
    public String copyNodes(Map<String, String> nodeMap) throws RepositoryException {
        return null;
    }

    @Override
    public String deleteNode(String sourcePath) throws RepositoryException {
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
    public String saveNodeDetails(String sourcePath, JcrNode jcrNode) throws RepositoryException {
        return "";
    }

    // Properties
    @Override
    public String addNewProperty(String sourcePath, String name, JcrProperty value) throws RepositoryException {
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
    public String deleteProperty(String sourcePath, String name) throws RepositoryException {
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
    public String saveProperties(String sourcePath, JcrNode jcrNode) throws RepositoryException {
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
    public String saveProperty(String sourcePath, String property, JcrProperty value) throws RepositoryException {
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

    public String savePropertyBinaryValue(String sourcePath, String property, InputStream inputStream) throws RepositoryException {
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
    public Boolean addNodeTypes(String cnd) throws RepositoryException {
        // return new UnsupportedOperationException("Not implemented yet");
        return null;
    }

    @Override
    public Boolean changeNodeTypeIconAssociation(String nodeType, String iconPath) throws RepositoryException {
        // return new UnsupportedOperationException("Not implemented yet");
        return null;
    }
}
