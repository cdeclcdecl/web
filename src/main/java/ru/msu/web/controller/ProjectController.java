package ru.msu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.web.dao.AssignmentDao;
import ru.msu.web.dao.EmployeeDao;
import ru.msu.web.dao.PositionDao;
import ru.msu.web.dao.ProjectDao;
import ru.msu.web.entity.Project;
import ru.msu.web.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectDao projectDao;
    private final AssignmentDao assignmentDao;
    private final EmployeeDao employeeDao;
    private final PositionDao positionDao;

    public ProjectController(ProjectDao projectDao, AssignmentDao assignmentDao,
                             EmployeeDao employeeDao, PositionDao positionDao) {
        this.projectDao = projectDao;
        this.assignmentDao = assignmentDao;
        this.employeeDao = employeeDao;
        this.positionDao = positionDao;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String startFrom,
                       @RequestParam(required = false) String startTo,
                       @RequestParam(required = false) String endFrom,
                       @RequestParam(required = false) String endTo,
                       @RequestParam(required = false, defaultValue = "nameAsc") String sort,
                       Model model) {
        LocalDate parsedStartFrom = parseDate(startFrom);
        LocalDate parsedStartTo = parseDate(startTo);
        LocalDate parsedEndFrom = parseDate(endFrom);
        LocalDate parsedEndTo = parseDate(endTo);
        List<Project> projects = projectDao.findAll().stream()
                .filter(project -> search == null || search.isBlank() || containsIgnoreCase(project.getProjectName(), search))
                .filter(project -> status == null || status.isBlank() || project.getProjectStatus().name().equals(status))
                .filter(project -> parsedStartFrom == null || !project.getStartDate().isBefore(parsedStartFrom))
                .filter(project -> parsedStartTo == null || !project.getStartDate().isAfter(parsedStartTo))
                .filter(project -> parsedEndFrom == null || project.getEndDate() != null && !project.getEndDate().isBefore(parsedEndFrom))
                .filter(project -> parsedEndTo == null || project.getEndDate() != null && !project.getEndDate().isAfter(parsedEndTo))
                .sorted(projectComparator(sort))
                .toList();
        model.addAttribute("projects", projects);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("startFrom", startFrom);
        model.addAttribute("startTo", startTo);
        model.addAttribute("endFrom", endFrom);
        model.addAttribute("endTo", endTo);
        model.addAttribute("sort", sort);
        model.addAttribute("statuses", ProjectStatus.values());
        return "projects/list";
    }

    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        Project project = projectDao.findById(id);
        if (project == null) return "redirect:/projects";
        model.addAttribute("project", project);
        model.addAttribute("assignments", assignmentDao.getCurrentProjectMembers(id));
        model.addAttribute("allAssignments", assignmentDao.getProjectAssignments(id));
        model.addAttribute("employees", employeeDao.findActiveEmployees());
        model.addAttribute("positions", positionDao.findAll());
        return "projects/view";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("statuses", ProjectStatus.values());
        return "projects/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String projectName,
                         @RequestParam String startDate,
                         @RequestParam(required = false) String endDate,
                         @RequestParam(required = false) String projectStatus,
                         Model model) {
        if (projectName == null || projectName.isBlank()) {
            model.addAttribute("error", "Название проекта обязательно для заполнения");
            model.addAttribute("project", new Project());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }
        if (projectDao.findByName(projectName) != null) {
            model.addAttribute("error", "Проект с таким названием уже существует");
            model.addAttribute("project", new Project());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }
        Project project = new Project();
        project.setProjectName(projectName);
        project.setStartDate(LocalDate.parse(startDate));
        project.setEndDate(endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null);
        project.setProjectStatus(projectStatus != null && !projectStatus.isBlank()
                ? ProjectStatus.valueOf(projectStatus) : ProjectStatus.planning);
        projectDao.save(project);
        return "redirect:/projects";
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Project project = projectDao.findById(id);
        if (project == null) return "redirect:/projects";
        model.addAttribute("project", project);
        model.addAttribute("statuses", ProjectStatus.values());
        return "projects/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String projectName,
                         @RequestParam String startDate,
                         @RequestParam(required = false) String endDate,
                         @RequestParam(required = false) String projectStatus,
                         Model model) {
        Project project = projectDao.findById(id);
        if (project == null) return "redirect:/projects";
        if (projectName == null || projectName.isBlank()) {
            model.addAttribute("error", "Название проекта обязательно для заполнения");
            model.addAttribute("project", project);
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }
        Project existing = projectDao.findByName(projectName);
        if (existing != null && !existing.getProjectId().equals(id)) {
            model.addAttribute("error", "Проект с таким названием уже существует");
            model.addAttribute("project", project);
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects/form";
        }
        project.setProjectName(projectName);
        project.setStartDate(LocalDate.parse(startDate));
        project.setEndDate(endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null);
        project.setProjectStatus(projectStatus != null && !projectStatus.isBlank()
                ? ProjectStatus.valueOf(projectStatus) : project.getProjectStatus());
        projectDao.save(project);
        return "redirect:/projects/" + id;
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private Comparator<Project> projectComparator(String sort) {
        Comparator<Project> comparator = Comparator.comparing(Project::getProjectName, String.CASE_INSENSITIVE_ORDER);
        if ("nameDesc".equals(sort)) {
            comparator = comparator.reversed();
        } else if ("startDateAsc".equals(sort)) {
            comparator = Comparator.comparing(Project::getStartDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("startDateDesc".equals(sort)) {
            comparator = Comparator.comparing(Project::getStartDate, Comparator.nullsLast(LocalDate::compareTo)).reversed();
        } else if ("endDateAsc".equals(sort)) {
            comparator = Comparator.comparing(Project::getEndDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("endDateDesc".equals(sort)) {
            comparator = Comparator.comparing(Project::getEndDate, Comparator.nullsLast(LocalDate::compareTo)).reversed();
        }
        return comparator;
    }
}
