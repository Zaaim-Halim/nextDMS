package io.nextdms.dms.explorer;

import io.nextdms.dto.explorer.JcrNode;
import io.nextdms.dto.explorer.JcrProperty;
import io.nextdms.dto.explorer.JcrValue;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
                Stream.of(node.getMixinNodeTypes()).map(NodeType::getName).toList(),
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

    public static void setNodeProperty(Node node, String name, JcrProperty prop) throws RepositoryException {
        if (prop.multiValue()) {
            Value[] values = Arrays.stream(prop.values())
                .map(jcrValue -> {
                    try {
                        return node.getSession().getValueFactory().createValue(jcrValue.getStringValue(), prop.type());
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(Value[]::new);
            node.setProperty(name, values);
        } else {
            JcrValue jcrValue = prop.values()[0];
            switch (prop.type()) {
                case PropertyType.BOOLEAN:
                    node.setProperty(name, jcrValue.getBooleanValue());
                    break;
                case PropertyType.DATE:
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(jcrValue.getDateValue());
                    node.setProperty(name, calendar);
                    break;
                case PropertyType.DECIMAL:
                    node.setProperty(name, jcrValue.getDecimalValue());
                    break;
                case PropertyType.DOUBLE:
                    node.setProperty(name, jcrValue.getDoubleValue());
                    break;
                case PropertyType.LONG:
                    node.setProperty(name, jcrValue.getLongValue());
                    break;
                case PropertyType.STRING:
                    node.setProperty(name, jcrValue.getStringValue());
                    break;
                default:
                    throw new RepositoryException("Unsupported property type: " + prop.type());
            }
        }
    }

    // TODO : add support for ordering (path, lastModified, etc)
    public static String transformTofullTextSearchNonExclusiveQuery(String query) {
        return String.format("SELECT * FROM [nt:base] WHERE CONTAINS(s.*, '%s')", query);
    }

    // TODO : add support for ordering (path, lastModified, etc)
    public static String transformToXPathSearchNonExclusiveQuery(String query, @NotBlank String targetPath) {
        return String.format("%s//element(*, nt:base)[jcr:contains(., '%s')]", targetPath, query);
    }

    // TODO : add support for ordering (path, lastModified, etc)
    public static String transformSqlSearchNonExclusiveQuery(String query, @NotBlank String targetPath) {
        return String.format("SELECT * FROM [nt:base] WHERE [nt:base] LIKE '%s%' AND CONTAINS(*, '%s')", targetPath, query);
    }

    public static QueryResult createQuery(QueryManager queryManager, String queryStr, String queryType, Pageable pageable)
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

    public static List<JcrNode> getSearcResult(Session session, QueryResult queryResult) throws RepositoryException {
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
                getProperties(node)
            );
            nodesList.add(jcrNode);
        }
        return nodesList;
    }

    public static <T> Page<T> transformToPage(List<T> content, Pageable pageable, int total) {
        if (pageable != null) {
            return new PageImpl<>(content, pageable, total);
        } else {
            return new PageImpl<>(content);
        }
    }

    public static int getSearchTotalCount(QueryManager queryManager, Pageable pageable, String queryType, String query)
        throws RepositoryException {
        if (pageable == null) {
            return 0;
        } else {
            return switch (queryType) {
                case Query.JCR_SQL2:
                    Query sqlQuery = queryManager.createQuery(transformToCountQuery(query), queryType);
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

    public static String transformToCountQuery(String query) {
        // Regular expression pattern to match 'SELECT' (case-insensitive),
        // followed by any characters, and then 'FROM' (case-insensitive).
        String regex = "(?i)SELECT\\s+.*?\\s+FROM";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);
        // Replace the part between SELECT and FROM with newSelection
        return matcher.replaceFirst("SELECT COUNT(*) FROM");
    }
}
