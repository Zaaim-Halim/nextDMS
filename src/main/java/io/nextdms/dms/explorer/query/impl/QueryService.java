package io.nextdms.dms.explorer.query.impl;

import static io.nextdms.dms.explorer.ExplorerUtils.*;
import static io.nextdms.dms.explorer.ExplorerUtils.getSearchTotalCount;

import io.nextdms.dms.explorer.query.IQueryService;
import io.nextdms.dto.explorer.JcrNode;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QueryService implements IQueryService {

    @Override
    public Page<JcrNode> search(Session session, String query, String type, Pageable pageable) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = createQuery(queryManager, query, Query.JCR_SQL2, pageable);
        return transformToPage(
            getSearcResult(session, result),
            pageable,
            getSearchTotalCount(queryManager, pageable, Query.JCR_SQL2, query)
        );
    }
}
