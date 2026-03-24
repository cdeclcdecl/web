package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.Employee;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDaoTest extends AbstractDaoTest {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private AssignmentDao assignmentDao;

    // findAll
    @Test
    void findAll_returnsAllEmployees() {
        List<Employee> all = employeeDao.findAll();
        assertEquals(55, all.size());
    }

    // findActiveEmployees
    @Test
    void findActiveEmployees_returnsOnlyActive() {
        List<Employee> active = employeeDao.findActiveEmployees();
        assertEquals(49, active.size());
        assertTrue(active.stream().allMatch(Employee::isActive));
    }

    // findFormerEmployees
    @Test
    void findFormerEmployees_returnsOnlyFired() {
        List<Employee> former = employeeDao.findFormerEmployees();
        assertEquals(6, former.size());
        assertTrue(former.stream().noneMatch(Employee::isActive));
    }

    // searchByName
    @Test
    void searchByName_found() {
        List<Employee> result = employeeDao.searchByName("Иванов Александр");
        assertEquals(1, result.size());
        assertEquals("Иванов Александр Михайлович", result.get(0).getFullName());
    }

    @Test
    void searchByName_notFound() {
        assertTrue(employeeDao.searchByName("xyz_нет_такого").isEmpty());
    }

    // findBySeniorityRange
    @Test
    void findBySeniorityRange_found() {
        long ceoSeniority = ChronoUnit.YEARS.between(LocalDate.of(2018, 1, 10), LocalDate.now());
        List<Employee> result = employeeDao.findBySeniorityRange((int) ceoSeniority, (int) ceoSeniority);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(Employee::isActive));
    }

    @Test
    void findBySeniorityRange_upperBound() {
        List<Employee> result = employeeDao.findBySeniorityRange(3, 5);
        assertFalse(result.isEmpty());
        assertTrue(result.size() < employeeDao.findActiveEmployees().size());
    }

    @Test
    void findBySeniorityRange_notFound() {
        assertTrue(employeeDao.findBySeniorityRange(100, 200).isEmpty());
    }

    // findByCurrentPosition
    @Test
    void findByCurrentPosition_found() {
        List<Employee> result = employeeDao.findByCurrentPosition(1L);
        assertEquals(1, result.size());
        assertEquals("Иванов Александр Михайлович", result.get(0).getFullName());
    }

    @Test
    void findByCurrentPosition_notFound() {
        assertTrue(employeeDao.findByCurrentPosition(9L).isEmpty());
    }

    // findByProject
    @Test
    void findByProject_found() {
        List<Employee> result = employeeDao.findByProject(1L);
        assertTrue(result.size() >= 5);
    }

    @Test
    void findByProject_notFound() {
        assertTrue(employeeDao.findByProject(20L).isEmpty());
    }

    // findById
    @Test
    void findById_found() {
        Employee emp = employeeDao.findById(1L);
        assertNotNull(emp);
        assertEquals("Иванов Александр Михайлович", emp.getFullName());
        assertEquals("ceo@nettech.ru", emp.getEmail());
        assertEquals(LocalDate.of(2018, 1, 10), emp.getHireDate());
        assertTrue(emp.isActive());
        long expected = ChronoUnit.YEARS.between(LocalDate.of(2018, 1, 10), LocalDate.now());
        assertEquals(expected, emp.getSeniorityYears());
    }

    @Test
    void findById_notFound() {
        assertNull(employeeDao.findById(9999L));
    }

    // findByEmail
    @Test
    void findByEmail_found() {
        Employee emp = employeeDao.findByEmail("cto@nettech.ru");
        assertNotNull(emp);
        assertEquals("Петров Дмитрий Сергеевич", emp.getFullName());
    }

    @Test
    void findByEmail_notFound() {
        assertNull(employeeDao.findByEmail("nonexistent@email.com"));
    }

    // save
    @Test
    void save_createsNewEmployee() {
        Employee emp = new Employee();
        emp.setFullName("Тестов Тест Тестович");
        emp.setAddress("г. Москва");
        emp.setBirthDate(LocalDate.of(1995, 1, 1));
        emp.setHireDate(LocalDate.of(2024, 1, 1));
        emp.setEmail("test@msu.ru");
        Employee saved = employeeDao.save(emp);
        assertNotNull(saved.getEmployeeId());

        Employee found = employeeDao.findById(saved.getEmployeeId());
        assertNotNull(found);
        assertEquals("test@msu.ru", found.getEmail());
        assertTrue(found.isActive());
    }

    // fireEmployee
    @Test
    void fireEmployee_setsLeaveDateAndClosesAssignments() {
        Employee before = employeeDao.findById(9L);
        assertTrue(before.isActive());
        assertFalse(assignmentDao.getCurrentEmployeeAssignments(9L).isEmpty());

        LocalDate fireDate = LocalDate.of(2025, 6, 30);
        employeeDao.fireEmployee(9L, fireDate);

        Employee after = employeeDao.findById(9L);
        assertFalse(after.isActive());
        assertEquals(fireDate, after.getLeaveDate());
        assertEquals(5, after.getSeniorityYears());
        assertTrue(assignmentDao.getCurrentEmployeeAssignments(9L).isEmpty());
    }
}
