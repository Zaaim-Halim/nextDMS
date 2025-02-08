package io.nextdms.app.config;

import io.nextdms.dms.config.OakConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScans({ @ComponentScan("io.nextdms.dms.*"), @ComponentScan("io.nextdms.app.*") })
@Import({ OakConfiguration.class })
public class ModulesConfiguration {}
