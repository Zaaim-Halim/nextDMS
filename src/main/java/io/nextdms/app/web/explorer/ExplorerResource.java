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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/full-text-search")
    public ResponseEntity<?> fullTextSearch(@RequestParam("query") @NotBlank(message = "query must not be blanck!") String query) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Fetching full text search for query: {}", query);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.fullTextSearch(session, ExplorerUtils.transformTofullTextSearchNonExclusiveQuery(query));
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.fullTextSearch");
        }
    }

    @RequestMapping("/x-path-search")
    public ResponseEntity<?> xpathSearch(
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
                ExplorerUtils.transformToXPathSearchNonExclusiveQuery(query, targetPath)
            );
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.xpathSearch");
        }
    }

    @RequestMapping("/sql-search")
    public ResponseEntity<?> sqlSearch(
        @RequestParam("query") @NotBlank(message = "query must not be blanck!") String query,
        @RequestParam("targetPath") @NotBlank(message = "target  path must not be blanck!") String targetPath
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching sql search for query: {} under path : {}", query, targetPath);
        }
        try {
            Session session = SessionUtils.getSessionForExplorer(repository, oakProperties);
            final var result = explorerReadService.sqlSearch(session, ExplorerUtils.transformSqlSearchNonExclusiveQuery(query, targetPath));
            SessionUtils.ungetSession(session);
            return ResponseEntity.ok(result);
        } catch (RepositoryException e) {
            throw new BadRequestAlertException(e.getMessage(), "explorer", "explorer.error.failed.fertch.xpathSearch");
        }
    }
}
