package io.nextdms.dms.config;

import jakarta.validation.constraints.NotNull;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.SecurityConfiguration;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;

public class OakSecurityProvider implements SecurityProvider {

    @Override
    public @NotNull ConfigurationParameters getParameters(@NotNull String s) {
        return null;
    }

    @NotNull
    @Override
    public Iterable<? extends SecurityConfiguration> getConfigurations() {
        return null;
    }

    @Override
    public <T> @NotNull T getConfiguration(@NotNull Class<T> aClass) {
        return null;
    }
}
