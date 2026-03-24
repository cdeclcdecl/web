package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.*;
import ru.msu.web.repository.AssignmentRepository;
import ru.msu.web.repository.EmployeeRepository;
import ru.msu.web.repository.PositionRepository;
import ru.msu.web.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.List;

// DAO для работы с назначениями сотрудников

@Service
public class AssignmentDao {

    private final AssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final PositionRepository positionRepository;

    public AssignmentDao(AssignmentRepository assignmentRepository,
                         EmployeeRepository employeeRepository,
                         ProjectRepository projectRepository,
                         PositionRepository positionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.positionRepository = positionRepository;
    }

    /* Все назначения сотрудника */
    public List<Assignment> getEmployeeAssignments(Long employeeId) {
        return assignmentRepository.findAllAssignmentsByEmployeeId(employeeId);
    }

    /* Текущие назначения сотрудника */
    public List<Assignment> getCurrentEmployeeAssignments(Long employeeId) {
        return assignmentRepository.findCurrentAssignmentsByEmployeeId(employeeId);
    }

    /* Все назначения на проект (+ архивные) */
    public List<Assignment> getProjectAssignments(Long projectId) {
        return assignmentRepository.findAllAssignmentsByProjectId(projectId);
    }

    /* Текущие участники проекта */
    public List<Assignment> getCurrentProjectMembers(Long projectId) {
        return assignmentRepository.findCurrentAssignmentsByProjectId(projectId);
    }

    /* Назначение сотрудника на проект с указанной должностью */
    public Assignment assignToProject(Long employeeId, Long projectId, Long positionId,
                                      int weeklyHours, LocalDate startDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + positionId));

        Assignment assignment = new Assignment();
        assignment.setEmployee(employee);
        assignment.setProject(project);
        assignment.setPosition(position);
        assignment.setWeeklyHours(weeklyHours);
        assignment.setStartDate(startDate);
        return assignmentRepository.save(assignment);
    }

    /* Завершение назначения  */
    public void removeFromProject(Long assignmentId, LocalDate endDate) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
        assignment.setEndDate(endDate);
        assignmentRepository.save(assignment);
    }

    /* Получить назначение по ID */
    public Assignment findById(Long id) {
        return assignmentRepository.findById(id).orElse(null);
    }
}
