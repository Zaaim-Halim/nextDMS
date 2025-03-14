package io.nextdms.app.web.explorer;

import io.nextdms.app.web.rest.errors.BadRequestAlertException;
import io.nextdms.dms.SessionUtils;
import io.nextdms.dms.config.OakProperties;
import io.nextdms.dms.explorer.ExplorerUtils;
import io.nextdms.dms.explorer.IExplorerReadService;
import io.nextdms.dms.explorer.IExplorerWriteService;
import jakarta.validation.constraints.NotBlank;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final OakProperties oakProperties;
    private final Repository repository;

    public ExplorerResource(
        IExplorerReadService explorerReadService,
        IExplorerWriteService explorerWriteService,
        OakProperties oakProperties,
        Repository repository
    ) {
        this.explorerReadService = explorerReadService;
        this.explorerWriteService = explorerWriteService;
        this.oakProperties = oakProperties;
        this.repository = repository;
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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.root");
        }
    }

    /**
     * Fetching node children
     * @param path
     * @return
     */
    @RequestMapping("/node/childreen")
    public ResponseEntity<?> nodeChildren(@RequestParam("path") String path) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching childreen for path: {}", path);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.getNode(session, path);
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.nodeChilderen");
        }
    }

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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.availableNodeTypes");
        }
    }

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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.mixinNodeTypes");
        }
    }

    /**
     * Full text search
     * @param pageable
     * @param query
     * @return
     */
    @RequestMapping("/full-text-search")
    public ResponseEntity<?> fullTextSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blanck!") String query
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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.fullTextSearch");
        }
    }

    /**
     * Xpath search
     * @param pageable
     * @param query
     * @param targetPath
     * @return
     */
    @RequestMapping("/x-path-search")
    public ResponseEntity<?> xpathSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blanck!") String query,
        @RequestParam("targetPath") @NotBlank(message = "target  path must not be blanck!") String targetPath
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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.xpathSearch");
        }
    }

    /**
     * SQL search. might not be used in the explorer itself but can be used in the future
     * @param pageable
     * @param query
     * @param targetPath
     * @return
     */
    @RequestMapping("/sql-search")
    public ResponseEntity<?> sqlSearch(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam("query") @NotBlank(message = "query must not be blanck!") String query,
        @RequestParam("targetPath") @NotBlank(message = "target  path must not be blanck!") String targetPath
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
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.xpathSearch");
        }
    }
}
