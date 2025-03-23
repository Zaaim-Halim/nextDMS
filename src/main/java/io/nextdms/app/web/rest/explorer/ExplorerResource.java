package io.nextdms.app.web.rest.explorer;

import io.nextdms.app.web.rest.errors.BadRequestAlertException;
import io.nextdms.app.web.rest.explorer.dto.NodeDto;
import io.nextdms.app.web.rest.explorer.dto.SearchDto;
import io.nextdms.dms.SessionUtils;
import io.nextdms.dms.config.OakProperties;
import io.nextdms.dms.explorer.ExplorerUtils;
import io.nextdms.dms.explorer.IExplorerReadService;
import io.nextdms.dms.explorer.IExplorerWriteService;
import io.nextdms.dms.explorer.query.IQueryService;
import io.nextdms.dto.NodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * Explorer resource : This class is responsible for handling all the explorer related operations
 * like fetching node tree, fetching node children, full text search, xpath search, sql search etc.
 */
@RestController
@RequestMapping("/api/explorer")
public class ExplorerResource {

    Logger LOG = org.slf4j.LoggerFactory.getLogger(ExplorerResource.class);
    private final IExplorerReadService explorerReadService;
    private final IExplorerWriteService explorerWriteService;
    private final IQueryService queryService;
    private final OakProperties oakProperties;
    private final Repository repository;

    public ExplorerResource(
        IExplorerReadService explorerReadService,
        IExplorerWriteService explorerWriteService,
        IQueryService queryService,
        OakProperties oakProperties,
        Repository repository
    ) {
        this.explorerReadService = explorerReadService;
        this.explorerWriteService = explorerWriteService;
        this.queryService = queryService;
        this.oakProperties = oakProperties;
        this.repository = repository;
    }

    /**
     * Fetching supported node types
     * @return List<NodeType>
     */
    @GetMapping("/supported-node-types")
    public ResponseEntity<?> supportedNodeTypes() {
        return ResponseEntity.ok(NodeType.values());
    }

    /**
     * Fetching root node
     * @return
     */
    @RequestMapping("/list-root")
    public ResponseEntity<?> listRoot() {
        if (LOG.isDebugEnabled()) {
            LOG.info("Request to list root");
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getNodeTree(session, "/");
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.root");
        }
    }

    /**
     * Fetching node tree
     * @param nodeDto : path : path of the node, UUID : UUID of the node
     * @return
     */
    @RequestMapping("/node/childreen")
    public ResponseEntity<?> nodeChildren(@Valid @org.springdoc.core.annotations.ParameterObject NodeDto nodeDto) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching childreen for node : {}", nodeDto.toString());
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getNode(session, nodeDto.path(), nodeDto.UUID());
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.nodeChilderen");
        }
    }

    /**
     * Fetching node properties
     * @param nodeDto : path : path of the node, UUID : UUID of the node
     * @return
     */
    @RequestMapping("/node/properties")
    public ResponseEntity<?> getNodeProperties(@Valid @org.springdoc.core.annotations.ParameterObject NodeDto nodeDto) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching properties for node : {}", "");
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getProperties(session, nodeDto.path(), nodeDto.UUID());
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.mixinNodeTypes");
        }
    }

    /**
     * Fetching available node types
     * @return
     */
    @RequestMapping("/availables-node-types")
    public ResponseEntity<?> availableNodeTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching available node types");
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getAvailableNodeTypes(session);
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.availableNodeTypes");
        }
    }

    /**
     * Fetching available mixin node types
     * @return
     */
    @RequestMapping("/availables-node-mixin-types")
    public ResponseEntity<?> availableMixinTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching available mixin node types");
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getMixinNodeTypes(session);
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.mixinNodeTypes");
        }
    }

    /**
     *
     * @param pageable
     * @param searchDto : query : a valid xpath, JCR-SQL2  query , type : query type supported values are : xpath, JCR-SQL2
     * @return
     */
    @RequestMapping("/search")
    public ResponseEntity<?> search(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @org.springdoc.core.annotations.ParameterObject SearchDto searchDto
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.info("search for query: ' {} ' of type '{}'", searchDto.query(), searchDto.type());
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = queryService.search(session, searchDto.query(), searchDto.type(), pageable);
            SessionUtils.ungetSession(session);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);
            return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.search");
        }
    }

    /**
     * Full text search
     * @param pageable
     * @param query : jsut a text not an actual full text search query
     * @return
     */
    @RequestMapping("/full-text-search")
    public ResponseEntity<?> fullTextSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blank!") String query
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching full text search for query: {}", query);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.fullTextSearch(
                session,
                ExplorerUtils.transformTofullTextSearchNonExclusiveQuery(query),
                pageable
            );
            SessionUtils.ungetSession(session);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);
            return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.fullTextSearch");
        }
    }

    /**
     * Xpath search
     * @param pageable
     * @param query : the query here is just a text not an actual xpath query
     * @param targetPath
     * @return
     */
    @RequestMapping("/x-path-search")
    public ResponseEntity<?> xpathSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blank!") String query,
        @RequestParam("targetPath") @NotBlank(message = "target  path must not be blank!") String targetPath
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching xpath search for query: {}", query);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.xpathSearch(
                session,
                ExplorerUtils.transformToXPathSearchNonExclusiveQuery(query, targetPath),
                pageable
            );
            SessionUtils.ungetSession(session);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);
            return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.xpathSearch");
        }
    }

    /**
     * SQL search. might not be used in the explorer itself but can be used in the future
     * @param pageable
     * @param query : just a text not an actual sql query
     * @param targetPath
     * @return
     */
    @RequestMapping("/sql-search")
    public ResponseEntity<?> sqlSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blank!") String query,
        @RequestParam("targetPath") @NotBlank(message = "target  path must not be blank!") String targetPath
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching sql search for query: {} under path : {}", query, targetPath);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.sqlSearch(
                session,
                ExplorerUtils.transformSqlSearchNonExclusiveQuery(query, targetPath),
                pageable
            );
            SessionUtils.ungetSession(session);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);
            return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fetch.xpathSearch");
        }
    }
}
