package ru.msu.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.msu.web.entity.Project;
import ru.msu.web.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByProjectStatus(ProjectStatus status);

    Optional<Project> findByProjectName(String name);

    List<Project> findByProjectNameContainingIgnoreCase(String fragment);

    List<Project> findByStartDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT p FROM Project p WHERE p.projectStatus = ru.msu.web.entity.ProjectStatus.active AND p.endDate IS NULL")
    List<Project> findActiveProjectsWithoutEndDate();
}
