package io.nextdms.app.web.rest.content;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Application operations.
 * an application is a special type of folder that can have many files and sub folders.
 * access to application can be restricted to a group of users.
 */
@RestController
@RequestMapping("/api/content/files")
public class AppResource {}
