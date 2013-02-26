package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.settings.ImmutableSettings;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.*;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 */
public class HttpBasicServerPlugin extends AbstractPlugin {

    private boolean enabledByDefault = true;
    private final Settings settings;

    @Inject public HttpBasicServerPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override public String name() {
        return "http-basic-server-plugin";
    }

    @Override public String description() {
        return "HTTP Basic Server Plugin";
    }

    @Override public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            modules.add(HttpBasicServerModule.class);
        }
        return modules;
    }

    @Override public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            services.add(HttpBasicServer.class);
        }
        return services;
    }

    @Override public Settings additionalSettings() {
        Loggers.getLogger(getClass()).info("now {}", settings);
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            return ImmutableSettings.settingsBuilder().
                    put("http.basic.user", "admin").
                    put("http.basic.password", "admin_pw").
                    put("http.enabled", false).                    
                    build();
        } else {
            return ImmutableSettings.Builder.EMPTY_SETTINGS;
        }
    }
}
