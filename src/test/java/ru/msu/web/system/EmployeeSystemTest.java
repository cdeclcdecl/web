package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void listEmployees_showsTable() {
        driver.get(url("/employees"));
        assertTrue(driver.getPageSource().contains("Иванов Александр"));
    }

    @Test
    void viewEmployee_showsDetails() {
        driver.get(url("/employees/1"));
        String src = driver.getPageSource();
        assertTrue(src.contains("ceo@nettech.ru"));
        assertTrue(src.contains("Иванов Александр Михайлович"));
    }

    @Test
    void addEmployee_success() {
        driver.get(url("/employees/new"));
        driver.findElement(By.name("fullName")).sendKeys("Системный Тест Тестович");
        driver.findElement(By.name("email")).sendKeys("systemtest_unique@test.ru");
        driver.findElement(By.name("birthDate")).sendKeys("1990-01-01");
        driver.findElement(By.name("hireDate")).sendKeys("2024-01-01");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/employees"));
        assertTrue(driver.getPageSource().contains("Системный Тест Тестович"));
    }

    @Test
    void addEmployee_missingName() {
        driver.get(url("/employees/new"));
        driver.findElement(By.name("birthDate")).sendKeys("1990-01-01");
        driver.findElement(By.name("hireDate")).sendKeys("2024-01-01");

        WebElement nameField = driver.findElement(By.name("fullName"));
        nameField.clear();

        driver.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement error = webWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(error.getText().contains("ФИО обязательно"));
    }

    @Test
    void addEmployee_duplicateEmail() {
        driver.get(url("/employees/new"));
        driver.findElement(By.name("fullName")).sendKeys("Дубликат Тест");
        driver.findElement(By.name("email")).sendKeys("ceo@nettech.ru");
        driver.findElement(By.name("birthDate")).sendKeys("1990-01-01");
        driver.findElement(By.name("hireDate")).sendKeys("2024-01-01");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        assertTrue(driver.getPageSource().contains("уже существует"));
    }

    @Test
    void addEmployee_missingBirthDate() {
        driver.get(url("/employees/new"));
        driver.findElement(By.name("fullName")).sendKeys("Тест Без Даты");
        driver.findElement(By.name("hireDate")).sendKeys("2024-01-01");

        WebElement birthField = driver.findElement(By.name("birthDate"));
        birthField.clear();

        driver.findElement(By.cssSelector("button[type=submit]")).click();

        WebElement error = webWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(error.getText().contains("Дата рождения обязательна"));
    }

    @Test
    void fireEmployee_success() {
        driver.get(url("/employees/5"));
        assertTrue(driver.getPageSource().contains("Работает"));

        WebElement dateInput = driver.findElement(By.name("leaveDate"));
        dateInput.sendKeys(LocalDate.now().toString());
        driver.findElement(By.cssSelector("button.btn-danger")).click();

        webWait().until(ExpectedConditions.urlContains("/employees/5"));
        assertTrue(driver.getPageSource().contains("Уволен"));
    }

    @Test
    void editEmployee_success() {
        String createdName = "System Employee Edit Source";
        String updatedName = "System Employee Edit Result";

        driver.get(url("/employees/new"));
        driver.findElement(By.name("fullName")).sendKeys(createdName);
        driver.findElement(By.name("email")).sendKeys("system-employee-edit@test.ru");
        driver.findElement(By.name("birthDate")).sendKeys("1991-02-03");
        driver.findElement(By.name("hireDate")).sendKeys("2024-02-03");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/employees"));
        driver.findElement(By.xpath("//tr[td/a[contains(., '" + createdName + "')]]//a[contains(@href, '/edit')]")).click();

        webWait().until(ExpectedConditions.urlContains("/edit"));
        WebElement fullName = driver.findElement(By.name("fullName"));
        fullName.clear();
        fullName.sendKeys(updatedName);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/employees/"));
        assertTrue(driver.getPageSource().contains(updatedName));
    }

    @Test
    void listEmployees_filtersByAssignmentAndSortsByProjects() {
        driver.get(url("/employees?search=%D0%BE%D0%B2&status=active&seniorityMin=3&seniorityMax=10&positionId=10&projectId=13&sort=projectsDesc"));

        String src = driver.getPageSource();
        assertTrue(src.contains("Смирнов Алексей Дмитриевич"));
        assertTrue(src.contains("Соколов Павел Андреевич"));
        assertFalse(src.contains("Павлов Сергей Александрович"));
        assertTrue(src.indexOf("Смирнов Алексей Дмитриевич") < src.indexOf("Соколов Павел Андреевич"));
    }

    @Test
    void listEmployees_sortsByPosition() {
        driver.get(url("/employees?projectId=13&status=active&sort=positionAsc"));

        String src = driver.getPageSource();
        assertTrue(src.contains("Жукова Наталья Петровна"));
        assertTrue(src.contains("Кузнецова Мария Ивановна"));
        assertTrue(src.indexOf("Жукова Наталья Петровна") < src.indexOf("Кузнецова Мария Ивановна"));
    }
}
