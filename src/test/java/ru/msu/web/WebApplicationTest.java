package ru.msu.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebApplicationTest {

    @Test
    void configure_registersApplicationClass() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        SpringApplicationBuilder configured = new WebApplication().configure(builder);

        assertTrue(configured.build().getAllSources().contains(WebApplication.class));
    }
}