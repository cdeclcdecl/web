package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.Assignment;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentDaoTest extends AbstractDaoTest {

    @Autowired
    private AssignmentDao assignmentDao;

    // getEmployeeAssignments
    @Test
    void getEmployeeAssignments_found() {
        List<Assignment> result = assignmentDao.getEmployeeAssignments(9L);
        assertEquals(2, result.size());
        Assignment ended = result.stream().filter(a -> a.getEndDate() != null).findFirst().orElseThrow();
        assertFalse(ended.isActive());
        assertEquals(12, ended.getDurationMonths());
    }

    @Test
    void getEmployeeAssignments_notFound() {
        assertTrue(assignmentDao.getEmployeeAssignments(9999L).isEmpty());
    }

    // getCurrentEmployeeAssignments
    @Test
    void getCurrentEmployeeAssignments_found() {
        List<Assignment> result = assignmentDao.getCurrentEmployeeAssignments(2L);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        long months = ChronoUnit.MONTHS.between(LocalDate.of(2018, 3, 15), LocalDate.now());
        assertEquals(months, result.get(0).getDurationMonths());
    }

    @Test
    void getCurrentEmployeeAssignments_notFound() {
        assertTrue(assignmentDao.getCurrentEmployeeAssignments(20L).isEmpty());
    }

    // getProjectAssignments
    @Test
    void getProjectAssignments_found() {
        List<Assignment> result = assignmentDao.getProjectAssignments(1L);
        assertTrue(result.size() >= 5);
    }

    @Test
    void getProjectAssignments_notFound() {
        assertTrue(assignmentDao.getProjectAssignments(20L).isEmpty());
    }

    // getCurrentProjectMembers
    @Test
    void getCurrentProjectMembers_found() {
        List<Assignment> result = assignmentDao.getCurrentProjectMembers(1L);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(Assignment::isActive));
    }

    @Test
    void getCurrentProjectMembers_notFound() {
        assertTrue(assignmentDao.getCurrentProjectMembers(20L).isEmpty());
    }

    // assignToProject
    @Test
    void assignToProject_valid() {
        Assignment a = assignmentDao.assignToProject(1L, 15L, 1L, 20, LocalDate.of(2025, 1, 1));
        assertNotNull(a.getAssignmentId());
        assertTrue(a.isActive());
        assertEquals(20, a.getWeeklyHours());
    }

    @Test
    void assignToProject_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                assignmentDao.assignToProject(9999L, 1L, 1L, 40, LocalDate.now()));
    }

    // removeFromProject
    @Test
    void removeFromProject_valid() {
        List<Assignment> active = assignmentDao.getCurrentEmployeeAssignments(9L);
        Assignment a = active.get(0);
        assignmentDao.removeFromProject(a.getAssignmentId(), LocalDate.of(2025, 3, 31));

        Assignment closed = assignmentDao.findById(a.getAssignmentId());
        assertFalse(closed.isActive());
        assertEquals(LocalDate.of(2025, 3, 31), closed.getEndDate());
    }

    @Test
    void removeFromProject_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                assignmentDao.removeFromProject(9999L, LocalDate.now()));
    }
}
