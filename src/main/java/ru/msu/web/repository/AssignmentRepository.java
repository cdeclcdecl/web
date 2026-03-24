package ru.msu.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.msu.web.entity.Assignment;
import ru.msu.web.entity.Employee;
import ru.msu.web.entity.Project;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByEmployee(Employee employee);

    List<Assignment> findByProject(Project project);

    List<Assignment> findByEmployeeAndEndDateIsNull(Employee employee);

    List<Assignment> findByProjectAndEndDateIsNull(Project project);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.employee JOIN FETCH a.position " +
           "WHERE a.project.projectId = :projectId AND a.endDate IS NULL")
    List<Assignment> findCurrentAssignmentsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.employee JOIN FETCH a.position " +
           "WHERE a.project.projectId = :projectId")
    List<Assignment> findAllAssignmentsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.project JOIN FETCH a.position " +
           "WHERE a.employee.employeeId = :employeeId AND a.endDate IS NULL")
    List<Assignment> findCurrentAssignmentsByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.project JOIN FETCH a.position " +
           "WHERE a.employee.employeeId = :employeeId")
    List<Assignment> findAllAssignmentsByEmployeeId(@Param("employeeId") Long employeeId);
}
