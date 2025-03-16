package io.nextdms.app.web.rest.content;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing CRUD folders operations.
 * Folders are used to organize files. in a tree structure.
 * Folders can have metadata and many files and sub folders.
 */
@RestController
@RequestMapping("/api/content/folders")
public class FoldersResource {}
