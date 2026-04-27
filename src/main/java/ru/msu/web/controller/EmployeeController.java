package ru.msu.web.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.web.dao.AssignmentDao;
import ru.msu.web.dao.EmployeeDao;
import ru.msu.web.dao.PositionDao;
import ru.msu.web.dao.ProjectDao;
import ru.msu.web.entity.Assignment;
import ru.msu.web.entity.Employee;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeDao employeeDao;
    private final AssignmentDao assignmentDao;
    private final PositionDao positionDao;
    private final ProjectDao projectDao;

    public EmployeeController(EmployeeDao employeeDao, AssignmentDao assignmentDao,
                              PositionDao positionDao, ProjectDao projectDao) {
        this.employeeDao = employeeDao;
        this.assignmentDao = assignmentDao;
        this.positionDao = positionDao;
        this.projectDao = projectDao;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false, defaultValue = "all") String status,
                       @RequestParam(required = false) Integer seniorityMin,
                       @RequestParam(required = false) Integer seniorityMax,
                       @RequestParam(required = false) Long positionId,
                       @RequestParam(required = false) Long projectId,
                       @RequestParam(required = false, defaultValue = "nameAsc") String sort,
                       Model model) {
        List<Employee> source = employeeDao.findAll();
        Map<Long, String> currentPositions = source.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, this::currentPosition));
        Map<Long, Long> projectCounts = source.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, this::projectCount));

        List<Employee> employees = source.stream()
                .filter(employee -> search == null || search.isBlank() || containsIgnoreCase(employee.getFullName(), search))
                .filter(employee -> !"active".equals(status) || employee.isActive())
                .filter(employee -> !"former".equals(status) || !employee.isActive())
                .filter(employee -> seniorityMin == null || employee.getSeniorityYears() >= seniorityMin)
                .filter(employee -> seniorityMax == null || employee.getSeniorityYears() <= seniorityMax)
                .filter(employee -> positionId == null || hasCurrentPosition(employee, positionId))
                .filter(employee -> projectId == null || hasProject(employee, projectId))
                .sorted(employeeComparator(sort, currentPositions, projectCounts))
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("currentPositions", currentPositions);
        model.addAttribute("projectCounts", projectCounts);
        model.addAttribute("positions", positionDao.findAll());
        model.addAttribute("projects", projectDao.findAll());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("seniorityMin", seniorityMin);
        model.addAttribute("seniorityMax", seniorityMax);
        model.addAttribute("positionId", positionId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("sort", sort);
        return "employees/list";
    }

    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        Employee emp = employeeDao.findById(id);
        if (emp == null) return "redirect:/employees";
        model.addAttribute("employee", emp);
        model.addAttribute("assignments", assignmentDao.getEmployeeAssignments(id));
        return "employees/view";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String fullName,
                         @RequestParam(required = false) String address,
                         @RequestParam String birthDate,
                         @RequestParam(required = false) String degree,
                         @RequestParam String hireDate,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phoneNumber,
                         Model model) {
        if (fullName == null || fullName.isBlank()) {
            model.addAttribute("error", "ФИО обязательно для заполнения");
            model.addAttribute("employee", new Employee());
            return "employees/form";
        }
        if (birthDate == null || birthDate.isBlank()) {
            model.addAttribute("error", "Дата рождения обязательна для заполнения");
            model.addAttribute("employee", new Employee());
            return "employees/form";
        }
        if (hireDate == null || hireDate.isBlank()) {
            model.addAttribute("error", "Дата найма обязательна для заполнения");
            model.addAttribute("employee", new Employee());
            return "employees/form";
        }
        if (email != null && !email.isBlank() && employeeDao.findByEmail(email) != null) {
            model.addAttribute("error", "Сотрудник с таким email уже существует");
            model.addAttribute("employee", new Employee());
            return "employees/form";
        }
        Employee emp = new Employee();
        emp.setFullName(fullName);
        emp.setAddress(address);
        emp.setBirthDate(LocalDate.parse(birthDate));
        emp.setDegree(degree);
        emp.setHireDate(LocalDate.parse(hireDate));
        emp.setEmail(email != null && !email.isBlank() ? email : null);
        emp.setPhoneNumber(phoneNumber);
        employeeDao.save(emp);
        return "redirect:/employees";
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee emp = employeeDao.findById(id);
        if (emp == null) return "redirect:/employees";
        model.addAttribute("employee", emp);
        return "employees/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String fullName,
                         @RequestParam(required = false) String address,
                         @RequestParam String birthDate,
                         @RequestParam(required = false) String degree,
                         @RequestParam String hireDate,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phoneNumber,
                         Model model) {
        Employee emp = employeeDao.findById(id);
        if (emp == null) return "redirect:/employees";
        if (fullName == null || fullName.isBlank()) {
            model.addAttribute("error", "ФИО обязательно для заполнения");
            model.addAttribute("employee", emp);
            return "employees/form";
        }
        if (birthDate == null || birthDate.isBlank()) {
            model.addAttribute("error", "Дата рождения обязательна для заполнения");
            model.addAttribute("employee", emp);
            return "employees/form";
        }
        if (hireDate == null || hireDate.isBlank()) {
            model.addAttribute("error", "Дата найма обязательна для заполнения");
            model.addAttribute("employee", emp);
            return "employees/form";
        }
        if (email != null && !email.isBlank()) {
            Employee existing = employeeDao.findByEmail(email);
            if (existing != null && !existing.getEmployeeId().equals(id)) {
                model.addAttribute("error", "Сотрудник с таким email уже существует");
                model.addAttribute("employee", emp);
                return "employees/form";
            }
        }
        emp.setFullName(fullName);
        emp.setAddress(address);
        emp.setBirthDate(LocalDate.parse(birthDate));
        emp.setDegree(degree);
        emp.setHireDate(LocalDate.parse(hireDate));
        emp.setEmail(email != null && !email.isBlank() ? email : null);
        emp.setPhoneNumber(phoneNumber);
        employeeDao.save(emp);
        return "redirect:/employees/" + id;
    }

    @PostMapping("/{id:\\d+}/fire")
    public String fire(@PathVariable Long id,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate leaveDate) {
        employeeDao.fireEmployee(id, leaveDate);
        return "redirect:/employees/" + id;
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private boolean hasCurrentPosition(Employee employee, Long positionId) {
        return employee.getAssignments().stream()
                .filter(Assignment::isActive)
                .anyMatch(assignment -> assignment.getPosition().getPositionId().equals(positionId));
    }

    private boolean hasProject(Employee employee, Long projectId) {
        return employee.getAssignments().stream()
                .anyMatch(assignment -> assignment.getProject().getProjectId().equals(projectId));
    }

    private String currentPosition(Employee employee) {
        String value = employee.getAssignments().stream()
                .filter(Assignment::isActive)
                .map(assignment -> assignment.getPosition().getName())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        return value.isBlank() ? "-" : value;
    }

    private long projectCount(Employee employee) {
        return employee.getAssignments().stream()
                .map(assignment -> assignment.getProject().getProjectId())
                .distinct()
                .count();
    }

    private Comparator<Employee> employeeComparator(String sort, Map<Long, String> currentPositions,
                                                    Map<Long, Long> projectCounts) {
        Comparator<Employee> comparator = Comparator.comparing(Employee::getFullName, String.CASE_INSENSITIVE_ORDER);
        if ("nameDesc".equals(sort)) {
            comparator = comparator.reversed();
        } else if ("hireDateAsc".equals(sort)) {
            comparator = Comparator.comparing(Employee::getHireDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("hireDateDesc".equals(sort)) {
            comparator = Comparator.comparing(Employee::getHireDate, Comparator.nullsLast(LocalDate::compareTo)).reversed();
        } else if ("birthDateAsc".equals(sort)) {
            comparator = Comparator.comparing(Employee::getBirthDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("birthDateDesc".equals(sort)) {
            comparator = Comparator.comparing(Employee::getBirthDate, Comparator.nullsLast(LocalDate::compareTo)).reversed();
        } else if ("positionAsc".equals(sort)) {
            comparator = Comparator.comparing(employee -> currentPositions.get(employee.getEmployeeId()), String.CASE_INSENSITIVE_ORDER);
        } else if ("projectsDesc".equals(sort)) {
            comparator = Comparator.comparing((Employee employee) -> projectCounts.get(employee.getEmployeeId())).reversed()
                    .thenComparing(Employee::getFullName, String.CASE_INSENSITIVE_ORDER);
        }
        return comparator;
    }
}
