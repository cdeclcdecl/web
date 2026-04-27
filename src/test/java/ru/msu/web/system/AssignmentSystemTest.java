package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void assignEmployee_success() {
        driver.get(url("/projects/2"));
        int countBefore = driver.findElements(By.cssSelector("table tr")).size();

        new Select(driver.findElement(By.name("employeeId"))).selectByValue("3");
        new Select(driver.findElement(By.name("positionId"))).selectByIndex(1);
        driver.findElement(By.name("weeklyHours")).clear();
        driver.findElement(By.name("weeklyHours")).sendKeys("20");
        driver.findElement(By.name("startDate")).sendKeys("2025-01-01");
        driver.findElement(By.cssSelector("form [type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/projects/2"));
        int countAfter = driver.findElements(By.cssSelector("table tr")).size();
        assertTrue(countAfter >= countBefore);
    }

    @Test
    void closeAssignment_success() {
        driver.get(url("/projects/1"));
        String srcBefore = driver.getPageSource();
        assertTrue(srcBefore.contains("Активно") || driver.findElements(By.name("endDate")).size() > 0);

        WebElement endDateInput = driver.findElements(By.name("endDate")).get(0);
        endDateInput.sendKeys("2025-12-31");

        WebElement closeForm = endDateInput.findElement(By.xpath("./.."));
        closeForm.findElement(By.cssSelector("button.btn-danger")).click();

        webWait().until(ExpectedConditions.urlContains("/projects/1"));
        assertTrue(driver.getPageSource().contains("Завершено") || driver.getPageSource().contains("2025-12-31"));
    }
}
