package ru.msu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.msu.web.dao.AssignmentDao;

import java.time.LocalDate;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentDao assignmentDao;

    public AssignmentController(AssignmentDao assignmentDao) {
        this.assignmentDao = assignmentDao;
    }

    @PostMapping
    public String assign(@RequestParam Long employeeId,
                         @RequestParam Long projectId,
                         @RequestParam Long positionId,
                         @RequestParam(defaultValue = "40") int weeklyHours,
                         @RequestParam String startDate) {
        assignmentDao.assignToProject(employeeId, projectId, positionId,
                weeklyHours, LocalDate.parse(startDate));
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id,
                        @RequestParam String endDate,
                        @RequestParam Long projectId,
                        @RequestParam(required = false) Long employeeId) {
        assignmentDao.removeFromProject(id, LocalDate.parse(endDate));
        if (employeeId != null) {
            return "redirect:/employees/" + employeeId;
        }
        return "redirect:/projects/" + projectId;
    }
}
