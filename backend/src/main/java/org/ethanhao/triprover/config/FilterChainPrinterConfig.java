package org.ethanhao.triprover.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.FilterChainProxy;

@Configuration
public class FilterChainPrinterConfig {

    @Autowired
    private FilterChainProxy filterChainProxy;

    @EventListener(ContextRefreshedEvent.class)
    public void printFilterChain() {
        filterChainProxy.getFilterChains().forEach(chain -> {
            System.out.println("Security filter chain:");
            chain.getFilters().forEach(filter -> {
                System.out.println("Filter: " + filter.getClass().getSimpleName());
            });
        });
    }
}
