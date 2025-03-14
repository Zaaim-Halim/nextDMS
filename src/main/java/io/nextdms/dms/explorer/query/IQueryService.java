package io.nextdms.dms.explorer.query;

import io.nextdms.dto.explorer.JcrNode;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IQueryService {
    Page<JcrNode> search(Session session, String query, String type, Pageable pageable) throws RepositoryException;
}
