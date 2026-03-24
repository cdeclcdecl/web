package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.Project;
import ru.msu.web.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDaoTest extends AbstractDaoTest {

    @Autowired
    private ProjectDao projectDao;

    // findAll
    @Test
    void findAll_returnsAllProjects() {
        List<Project> all = projectDao.findAll();
        assertEquals(20, all.size());
    }

    // findByStatus
    @Test
    void findByStatus_active() {
        List<Project> result = projectDao.findByStatus(ProjectStatus.active);
        assertEquals(13, result.size());
        assertTrue(result.stream().allMatch(Project::isActive));
        assertFalse(result.get(0).isCompleted());
    }

    @Test
    void findByStatus_completed() {
        List<Project> result = projectDao.findByStatus(ProjectStatus.completed);
        assertEquals(1, result.size());
        Project p = result.get(0);
        assertTrue(p.isCompleted());
        assertFalse(p.isActive());
        assertNotNull(p.getEndDate());
    }

    // searchByName
    @Test
    void searchByName_found() {
        List<Project> result = projectDao.searchByName("Analyzer");
        assertTrue(result.size() >= 2);
    }

    @Test
    void searchByName_notFound() {
        assertTrue(projectDao.searchByName("xyz_нет_такого").isEmpty());
    }

    // findByStartDateRange
    @Test
    void findByStartDateRange_found() {
        List<Project> result = projectDao.findByStartDateRange(
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        assertEquals(6, result.size());
    }

    @Test
    void findByStartDateRange_notFound() {
        assertTrue(projectDao.findByStartDateRange(
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31)).isEmpty());
    }

    // findById
    @Test
    void findById_found() {
        Project p = projectDao.findById(1L);
        assertNotNull(p);
        assertEquals("Руководство компании", p.getProjectName());
        assertEquals(ProjectStatus.active, p.getProjectStatus());
    }

    @Test
    void findById_notFound() {
        assertNull(projectDao.findById(9999L));
    }

    // findByName
    @Test
    void findByName_found() {
        Project p = projectDao.findByName("Руководство компании");
        assertNotNull(p);
        assertTrue(p.isActive());
    }

    @Test
    void findByName_notFound() {
        assertNull(projectDao.findByName("Несуществующий проект"));
    }

    // save
    @Test
    void save_createsNewProject() {
        Project p = new Project();
        p.setProjectName("Тестовый проект");
        p.setStartDate(LocalDate.of(2025, 1, 1));
        p.setProjectStatus(ProjectStatus.planning);
        Project saved = projectDao.save(p);
        assertNotNull(saved.getProjectId());

        Project found = projectDao.findById(saved.getProjectId());
        assertNotNull(found);
        assertEquals("Тестовый проект", found.getProjectName());
        assertEquals(ProjectStatus.planning, found.getProjectStatus());
    }
}
