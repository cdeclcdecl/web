package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PositionSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void listPositions_showsAll() {
        driver.get(url("/positions"));
        List<WebElement> rows = driver.findElements(By.cssSelector("table tr"));
        assertTrue(rows.size() > 30);
    }

    @Test
    void addPosition_success() {
        driver.get(url("/positions"));
        driver.findElement(By.name("name")).sendKeys("Тестовая должность уникальная 7z");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/positions"));
        assertTrue(driver.getPageSource().contains("Тестовая должность уникальная 7z"));
    }

    @Test
    void addPosition_duplicate() {
        driver.get(url("/positions"));
        driver.findElement(By.name("name")).sendKeys("Генеральный директор");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        assertTrue(driver.getPageSource().contains("уже существует"));
    }

    @Test
    void deletePosition_success() {
        driver.get(url("/positions"));
        driver.findElement(By.name("name")).sendKeys("Временная должность для удаления xyz");
        driver.findElement(By.cssSelector("button[type=submit]")).click();
        webWait().until(ExpectedConditions.urlContains("/positions"));

        assertTrue(driver.getPageSource().contains("Временная должность для удаления xyz"));

        List<WebElement> rows = driver.findElements(By.cssSelector("table tr"));
        WebElement deleteBtn = null;
        for (WebElement row : rows) {
            if (row.getText().contains("Временная должность для удаления xyz")) {
                deleteBtn = row.findElement(By.cssSelector("button.btn-danger"));
                break;
            }
        }
        assertNotNull(deleteBtn);
        deleteBtn.click();

        webWait().until(ExpectedConditions.urlContains("/positions"));
        assertFalse(driver.getPageSource().contains("Временная должность для удаления xyz"));
    }
}
