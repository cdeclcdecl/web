package ru.msu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.msu.web.dao.EmployeeDao;
import ru.msu.web.dao.PaymentDao;
import ru.msu.web.dao.PaymentPolicyDao;
import ru.msu.web.dao.ProjectDao;
import ru.msu.web.entity.ProjectStatus;

@Controller
public class HomeController {

    private final EmployeeDao employeeDao;
    private final ProjectDao projectDao;
    private final PaymentDao paymentDao;
    private final PaymentPolicyDao paymentPolicyDao;

    public HomeController(EmployeeDao employeeDao, ProjectDao projectDao,
                          PaymentDao paymentDao, PaymentPolicyDao paymentPolicyDao) {
        this.employeeDao = employeeDao;
        this.projectDao = projectDao;
        this.paymentDao = paymentDao;
        this.paymentPolicyDao = paymentPolicyDao;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("employeesTotal", employeeDao.findAll().size());
        model.addAttribute("activeEmployees", employeeDao.findActiveEmployees().size());
        model.addAttribute("projectsTotal", projectDao.findAll().size());
        model.addAttribute("activeProjects", projectDao.findByStatus(ProjectStatus.active).size());
        model.addAttribute("unpaidPayments", paymentDao.findUnpaid().size());
        model.addAttribute("policiesTotal", paymentPolicyDao.findAll().size());
        return "home";
    }
}
