package ru.msu.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.msu.web.entity.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByLeaveDateIsNull();

    List<Employee> findByLeaveDateIsNotNull();

    List<Employee> findByHireDateBetween(LocalDate from, LocalDate to);

    List<Employee> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.assignments a " +
           "WHERE a.endDate IS NULL AND a.position.positionId = :positionId")
    List<Employee> findByCurrentPositionId(@Param("positionId") Long positionId);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.assignments a " +
           "WHERE a.project.projectId = :projectId")
    List<Employee> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT e FROM Employee e WHERE e.leaveDate IS NULL AND e.hireDate <= :cutoffDate")
    List<Employee> findActiveEmployeesHiredBefore(@Param("cutoffDate") LocalDate cutoffDate);
}
