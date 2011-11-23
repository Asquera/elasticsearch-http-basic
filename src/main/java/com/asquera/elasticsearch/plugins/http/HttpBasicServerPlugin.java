package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.http.HttpServerModule;
import org.elasticsearch.http.HttpServer;
import com.asquera.elasticsearch.plugins.http.HttpBasicServer;
import com.asquera.elasticsearch.plugins.http.HttpBasicServerModule;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.settings.ImmutableSettings;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.*;

/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 */
public class HttpBasicServerPlugin extends AbstractPlugin {
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
        if (settings.getAsBoolean("http.basic.enabled", false)) {
            modules.add(HttpBasicServerModule.class);
        }
        return modules;
    }
    
    @Override public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean("http.basic.enabled", false)) {
            services.add(HttpBasicServer.class);
        }
        return services;
    }
    
    @Override public Settings additionalSettings() {
        if (settings.getAsBoolean("http.basic.enabled", false)) {
            return ImmutableSettings.settingsBuilder().put("http.enabled", false).build();
        } else {
            return ImmutableSettings.Builder.EMPTY_SETTINGS;
        }
    }
}
