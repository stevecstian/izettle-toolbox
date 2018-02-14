package com.izettle.dropwizard.filters;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class AdminDirectAccessBundle implements Bundle {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Environment environment) {
        ServletContextHandler adminContext = environment.getAdminContext();

        adminContext.addFilter(
            new FilterHolder(new AdminDirectAccessFilter(adminContext.getContextPath())),
            "*",
            EnumSet.allOf(DispatcherType.class)
        );
    }
}
