package io.nextdms.app.web.rest.content;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing CRUD files operations.
 * PDF, DOCX , images , videos , audios , documents , etc.
 * each file can be associated with a folder. and can have many versions, and metadata.
 */
@RestController
@RequestMapping("/api/content/files")
public class FilesResource {}
