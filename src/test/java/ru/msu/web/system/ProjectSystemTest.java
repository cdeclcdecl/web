package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ProjectSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void listProjects_showsAll() {
        driver.get(url("/projects"));
        assertTrue(driver.getPageSource().contains("Руководство компании"));
    }

    @Test
    void addProject_success() {
        driver.get(url("/projects/new"));
        driver.findElement(By.name("projectName")).sendKeys("Уникальный тестовый проект 9x");
        driver.findElement(By.name("startDate")).sendKeys("2025-01-01");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/projects"));
        assertTrue(driver.getPageSource().contains("Уникальный тестовый проект 9x"));
    }

    @Test
    void addProject_duplicateName() {
        driver.get(url("/projects/new"));
        driver.findElement(By.name("projectName")).sendKeys("Руководство компании");
        driver.findElement(By.name("startDate")).sendKeys("2025-01-01");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        assertTrue(driver.getPageSource().contains("уже существует"));
    }

    @Test
    void addProject_missingName() {
        driver.get(url("/projects/new"));
        driver.findElement(By.name("startDate")).sendKeys("2025-01-01");
        driver.findElement(By.cssSelector("form")).submit();

        String src = driver.getPageSource();
        assertTrue(src.contains("обязательно") || src.contains("required"));
    }

    @Test
    void editProject_success() {
        String createdName = "System Project Edit Source";
        String updatedName = "System Project Edit Result";

        driver.get(url("/projects/new"));
        driver.findElement(By.name("projectName")).sendKeys(createdName);
        driver.findElement(By.name("startDate")).sendKeys("2025-02-01");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/projects"));
        driver.findElement(By.xpath("//tr[td/a[contains(., '" + createdName + "')]]//a[contains(@href, '/edit')]")).click();

        webWait().until(ExpectedConditions.urlContains("/edit"));
        driver.findElement(By.name("projectName")).clear();
        driver.findElement(By.name("projectName")).sendKeys(updatedName);
        driver.findElement(By.name("endDate")).sendKeys("2025-12-31");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/projects/"));
        assertTrue(driver.getPageSource().contains(updatedName));
    }

    @Test
    void listProjects_filtersByDatesAndSortsByEndDate() {
        driver.get(url("/projects?search=SecureNet&startFrom=2023-01-01&startTo=2023-12-31&endFrom=2023-01-01&endTo=2023-12-31&sort=endDateDesc"));

        String src = driver.getPageSource();
        assertTrue(src.contains("SecureNet DDoS Protection - защита от DDoS атак"));
        assertFalse(src.contains("Kubernetes Network Policy Manager"));
    }
}
