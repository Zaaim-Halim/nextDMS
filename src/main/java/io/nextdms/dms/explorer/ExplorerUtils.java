package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import io.nextdms.dto.explorer.JcrValue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import javax.jcr.*;
import javax.jcr.nodetype.NodeType;

public class ExplorerUtils {

    public static List<JcrNode> getChildreen(Node node) throws RepositoryException {
        List<JcrNode> children = new ArrayList<>();
        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            JcrNode jcrNode = new JcrNode(
                childNode.getIdentifier(),
                childNode.getName(),
                childNode.getPath(),
                childNode.getPrimaryNodeType().getName(),
                List.of(node.getMixinNodeTypes()).stream().map(NodeType::getName).toList(),
                getProperties(childNode)
            );
            children.add(jcrNode);
        }
        return children;
    }

    public static Map<String, JcrProperty> getProperties(Node node) throws RepositoryException {
        Map<String, JcrProperty> properties = new HashMap<>();
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            JcrProperty jcrProperty = new JcrProperty(
                property.getName(),
                property.getType(),
                property.isMultiple(),
                property.getDefinition().isProtected(),
                getJcrValues(property)
            );
            properties.put(property.getName(), jcrProperty);
        }
        return properties;
    }

    static JcrValue[] getJcrValues(Property property) throws RepositoryException {
        if (property.isMultiple()) {
            Value[] values = property.getValues();
            JcrValue[] jcrValues = new JcrValue[values.length];
            for (int i = 0; i < values.length; i++) {
                jcrValues[i] = convertValue(values[i], values[i].getType());
            }
            return jcrValues;
        } else {
            Calendar calendar = property.getDate();
            Date date = (calendar != null) ? calendar.getTime() : null;
            LocalDate localDate = (calendar != null) ? calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            var jcrValue = new JcrValue(
                property.getBoolean(),
                date,
                localDate,
                null,
                null,
                property.getDecimal(),
                property.getDouble(),
                property.getLong(),
                property.getString(),
                true,
                PropertyType.nameFromValue(property.getType())
            );
            return new JcrValue[] { jcrValue };
        }
    }

    private static JcrValue convertValue(Value value, int type) throws ValueFormatException, RepositoryException {
        var jcrValue = new JcrValue();
        switch (type) {
            case PropertyType.BOOLEAN:
                jcrValue.setPropertyType(PropertyType.TYPENAME_BOOLEAN);
                jcrValue.setBooleanValue(value.getBoolean());
                break;
            case PropertyType.DATE:
                jcrValue.setPropertyType(PropertyType.TYPENAME_DATE);
                Calendar calendar = value.getDate();
                Date date = (calendar != null) ? calendar.getTime() : null;
                LocalDate localDate = (calendar != null) ? calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
                jcrValue.setDateValue(date);
                jcrValue.setLocalDateValue(localDate);
                break;
            case PropertyType.DECIMAL:
                jcrValue.setPropertyType(PropertyType.TYPENAME_DECIMAL);
                jcrValue.setDecimalValue(value.getDecimal());
                break;
            case PropertyType.DOUBLE:
                jcrValue.setPropertyType(PropertyType.TYPENAME_DOUBLE);
                jcrValue.setDoubleValue(value.getDouble());
                break;
            case PropertyType.LONG:
                jcrValue.setPropertyType(PropertyType.TYPENAME_LONG);
                jcrValue.setLongValue(value.getLong());
                break;
            case PropertyType.STRING:
                jcrValue.setPropertyType(PropertyType.TYPENAME_STRING);
                jcrValue.setStringValue(value.getString());
                break;
            case PropertyType.NAME:
            case PropertyType.PATH:
            case PropertyType.UNDEFINED:
            case PropertyType.URI:
            case PropertyType.BINARY:
            case PropertyType.WEAKREFERENCE:
            default:
                break;
        }
        return jcrValue;
    }
}
