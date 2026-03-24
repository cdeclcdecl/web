package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.Employee;
import ru.msu.web.repository.AssignmentRepository;
import ru.msu.web.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
 * DAO для работы с сотрудниками
 * Поддерживает операции страницы "Список сотрудников" и "Информация о сотруднике"
 */
@Service
public class EmployeeDao {

    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;

    public EmployeeDao(EmployeeRepository employeeRepository,
                       AssignmentRepository assignmentRepository) {
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /* Все сотрудники  */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /* Только действующие сотрудники */
    public List<Employee> findActiveEmployees() {
        return employeeRepository.findByLeaveDateIsNull();
    }

    /* Только уволенные сотрудники */
    public List<Employee> findFormerEmployees() {
        return employeeRepository.findByLeaveDateIsNotNull();
    }

    /* Поиск по ФИО */
    public List<Employee> searchByName(String query) {
        return employeeRepository.findByFullNameContainingIgnoreCase(query);
    }

    /* Фильтр по стажу */
    public List<Employee> findBySeniorityRange(int minYears, int maxYears) {
        return employeeRepository.findByLeaveDateIsNull().stream()
                .filter(e -> {
                    long years = e.getSeniorityYears();
                    return years >= minYears && years <= maxYears;
                })
                .collect(Collectors.toList());
    }

    /* Сотрудники, имеющие текущее назначение на указанную должность */
    public List<Employee> findByCurrentPosition(Long positionId) {
        return employeeRepository.findByCurrentPositionId(positionId);
    }

    /* Сотрудники, участвующие (или участвовавшие) в проекте */
    public List<Employee> findByProject(Long projectId) {
        return employeeRepository.findByProjectId(projectId);
    }

    /* Получить сотрудника по ID. Возвращает null, если не найден */
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    /* Получить сотрудника по email. Возвращает null, если не найден */
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email).orElse(null);
    }

    /* Сохранить (создать или обновить) сотрудника */
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    /* Увольнение сотрудника: установка даты увольнения и закрытие всех активных назначений */
    public void fireEmployee(Long employeeId, LocalDate leaveDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        employee.setLeaveDate(leaveDate);
        employeeRepository.save(employee);

        assignmentRepository.findByEmployeeAndEndDateIsNull(employee).forEach(a -> {
            a.setEndDate(leaveDate);
            assignmentRepository.save(a);
        });
    }
}
