package ru.msu.web.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeSystemTest extends AbstractSystemTest {

    @Test
    void dashboard_showsNavigation() {
        driver.get(url("/"));
        String src = driver.getPageSource();
        assertTrue(src.contains("Зарплатная ведомость"));
        assertTrue(src.contains("История начислений"));
        assertTrue(src.contains("Политики выплат"));
    }
}