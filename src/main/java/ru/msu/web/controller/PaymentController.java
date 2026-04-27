package ru.msu.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.msu.web.dao.AssignmentDao;
import ru.msu.web.dao.EmployeeDao;
import ru.msu.web.dao.PaymentDao;
import ru.msu.web.dao.PaymentPolicyDao;
import ru.msu.web.dao.ProjectDao;
import ru.msu.web.entity.Payment;
import ru.msu.web.entity.PolicyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentDao paymentDao;
    private final EmployeeDao employeeDao;
    private final AssignmentDao assignmentDao;
    private final PaymentPolicyDao paymentPolicyDao;
    private final ProjectDao projectDao;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentDao paymentDao, EmployeeDao employeeDao,
                             AssignmentDao assignmentDao, PaymentPolicyDao paymentPolicyDao,
                             ProjectDao projectDao, ObjectMapper objectMapper) {
        this.paymentDao = paymentDao;
        this.employeeDao = employeeDao;
        this.assignmentDao = assignmentDao;
        this.paymentPolicyDao = paymentPolicyDao;
        this.projectDao = projectDao;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                       @RequestParam(required = false) BigDecimal amountMin,
                       @RequestParam(required = false) BigDecimal amountMax,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) Long employeeId,
                       @RequestParam(required = false) Long policyId,
                       @RequestParam(required = false) Long projectId,
                       @RequestParam(required = false, defaultValue = "dateDesc") String sort,
                       Model model) {
        populateListModel(model, dateFrom, dateTo, amountMin, amountMax, type, employeeId, policyId, projectId, sort, false);
        return "payments/list";
    }

    @GetMapping("/print")
    public String print(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                        @RequestParam(required = false) BigDecimal amountMin,
                        @RequestParam(required = false) BigDecimal amountMax,
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) Long employeeId,
                        @RequestParam(required = false) Long policyId,
                        @RequestParam(required = false) Long projectId,
                        @RequestParam(required = false, defaultValue = "dateDesc") String sort,
                        Model model) {
        populateListModel(model, dateFrom, dateTo, amountMin, amountMax, type, employeeId, policyId, projectId, sort, true);
        return "payments/list";
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> export(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                         @RequestParam(required = false) BigDecimal amountMin,
                                         @RequestParam(required = false) BigDecimal amountMax,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) Long employeeId,
                                         @RequestParam(required = false) Long policyId,
                                         @RequestParam(required = false) Long projectId,
                                         @RequestParam(required = false, defaultValue = "dateDesc") String sort) {
        List<Payment> payments = filterPayments(dateFrom, dateTo, amountMin, amountMax, type, employeeId, policyId, projectId, sort);
        Map<Long, Map<String, String>> meta = buildPaymentMeta(payments);
        StringBuilder csv = new StringBuilder("id,employee,project,position,type,amount,paymentDate,status,reason\n");
        for (Payment payment : payments) {
            Map<String, String> values = meta.get(payment.getPaymentId());
            csv.append(payment.getPaymentId()).append(',')
                    .append(csv(values.get("employeeName"))).append(',')
                    .append(csv(values.get("projectName"))).append(',')
                    .append(csv(values.get("positionName"))).append(',')
                    .append(csv(values.get("typeLabel"))).append(',')
                    .append(payment.getAmount()).append(',')
                    .append(payment.getPaymentDate()).append(',')
                    .append(payment.isTransactioned() ? "paid" : "unpaid").append(',')
                    .append(csv(values.get("reason")))
                    .append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csv.toString());
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateForm(model, new Payment(), null, null);
        return "payments/form";
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Payment payment = paymentDao.findById(id);
        if (payment == null) {
            return "redirect:/payments";
        }
        Long selectedEmployeeId = payment.getAssignment() == null ? null : payment.getAssignment().getEmployee().getEmployeeId();
        Long selectedAssignmentId = payment.getAssignment() == null ? null : payment.getAssignment().getAssignmentId();
        populateForm(model, payment, selectedEmployeeId, selectedAssignmentId);
        return "payments/form";
    }

    @GetMapping("/new/assignments")
    @ResponseBody
    public Object assignmentsByEmployee(@RequestParam Long employeeId) {
        return assignmentDao.getEmployeeAssignments(employeeId);
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long employeeId,
                         @RequestParam(required = false) Long assignmentId,
                         @RequestParam(required = false) Long policyId,
                         @RequestParam String amount,
                         @RequestParam(required = false) String periodStart,
                         @RequestParam(required = false) String periodEnd,
                         @RequestParam(required = false) String paymentDate,
                         @RequestParam(defaultValue = "false") boolean transactioned,
                         Model model) {
        return savePayment(new Payment(), employeeId, assignmentId, policyId, amount, periodStart, periodEnd, paymentDate, transactioned, model);
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) Long employeeId,
                         @RequestParam(required = false) Long assignmentId,
                         @RequestParam(required = false) Long policyId,
                         @RequestParam String amount,
                         @RequestParam(required = false) String periodStart,
                         @RequestParam(required = false) String periodEnd,
                         @RequestParam(required = false) String paymentDate,
                         @RequestParam(defaultValue = "false") boolean transactioned,
                         Model model) {
        Payment payment = paymentDao.findById(id);
        if (payment == null) {
            return "redirect:/payments";
        }
        return savePayment(payment, employeeId, assignmentId, policyId, amount, periodStart, periodEnd, paymentDate, transactioned, model);
    }

    private String savePayment(Payment payment, Long employeeId, Long assignmentId, Long policyId,
                               String amount, String periodStart, String periodEnd,
                               String paymentDate, boolean transactioned, Model model) {
        if (amount == null || amount.isBlank()) {
            model.addAttribute("error", "Сумма обязательна для заполнения");
            populateForm(model, payment, employeeId, assignmentId);
            return "payments/form";
        }
        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Некорректная сумма выплаты");
            populateForm(model, payment, employeeId, assignmentId);
            return "payments/form";
        }
        payment.setAssignment(assignmentId == null ? null : assignmentDao.findById(assignmentId));
        payment.setPolicy(policyId == null ? null : paymentPolicyDao.findById(policyId));
        payment.setAmount(parsedAmount);
        payment.setPeriodStart(parseDate(periodStart));
        payment.setPeriodEnd(parseDate(periodEnd));
        payment.setPaymentDate(parseDate(paymentDate));
        payment.setTransactioned(transactioned);
        paymentDao.save(payment);
        return "redirect:/payments";
    }

    private void populateForm(Model model, Payment payment, Long selectedEmployeeId, Long selectedAssignmentId) {
        model.addAttribute("payment", payment);
        model.addAttribute("employees", employeeDao.findAll());
        model.addAttribute("policies", paymentPolicyDao.findAll());
        model.addAttribute("selectedEmployeeId", selectedEmployeeId);
        model.addAttribute("selectedAssignmentId", selectedAssignmentId);
        model.addAttribute("selectedPolicyId", payment.getPolicy() == null ? null : payment.getPolicy().getPolicyId());
    }

    private void populateListModel(Model model, LocalDate dateFrom, LocalDate dateTo,
                                   BigDecimal amountMin, BigDecimal amountMax, String type,
                                   Long employeeId, Long policyId, Long projectId,
                                   String sort, boolean printMode) {
        List<Payment> payments = filterPayments(dateFrom, dateTo, amountMin, amountMax, type, employeeId, policyId, projectId, sort);
        model.addAttribute("payments", payments);
        model.addAttribute("paymentMeta", buildPaymentMeta(payments));
        model.addAttribute("employees", employeeDao.findAll());
        model.addAttribute("policies", paymentPolicyDao.findAll());
        model.addAttribute("projects", projectDao.findAll());
        model.addAttribute("policyTypes", PolicyType.values());
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("amountMin", amountMin);
        model.addAttribute("amountMax", amountMax);
        model.addAttribute("type", type);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("policyId", policyId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("sort", sort);
        model.addAttribute("printMode", printMode);
        model.addAttribute("totalAmount", sum(payments));
        model.addAttribute("salaryTotal", sumByType(payments, PolicyType.salary));
        model.addAttribute("seniorityTotal", sumByType(payments, PolicyType.seniority));
        model.addAttribute("calendarBonusTotal", sumByType(payments, PolicyType.calendar_bonus));
        model.addAttribute("oneTimeBonusTotal", sumByType(payments, PolicyType.one_time_bonus));
        model.addAttribute("unpaidCount", payments.stream().filter(payment -> !payment.isTransactioned()).count());
        model.addAttribute("reportTitle", reportTitle(type, employeeId));
        if (employeeId != null) {
            model.addAttribute("averageMonthly", averageMonthly(sum(payments), dateFrom, dateTo));
        }
    }

    private List<Payment> filterPayments(LocalDate dateFrom, LocalDate dateTo,
                                         BigDecimal amountMin, BigDecimal amountMax, String type,
                                         Long employeeId, Long policyId, Long projectId, String sort) {
        return paymentDao.findAll().stream()
                .filter(payment -> dateFrom == null || payment.getPaymentDate() != null && !payment.getPaymentDate().isBefore(dateFrom))
                .filter(payment -> dateTo == null || payment.getPaymentDate() != null && !payment.getPaymentDate().isAfter(dateTo))
                .filter(payment -> amountMin == null || payment.getAmount().compareTo(amountMin) >= 0)
                .filter(payment -> amountMax == null || payment.getAmount().compareTo(amountMax) <= 0)
                .filter(payment -> type == null || type.isBlank() || payment.getPolicy() != null && payment.getPolicy().getPolicyType().name().equals(type))
                .filter(payment -> employeeId == null || payment.getAssignment() != null && payment.getAssignment().getEmployee().getEmployeeId().equals(employeeId))
                .filter(payment -> policyId == null || payment.getPolicy() != null && payment.getPolicy().getPolicyId().equals(policyId))
                .filter(payment -> projectId == null || payment.getAssignment() != null && payment.getAssignment().getProject().getProjectId().equals(projectId))
                .sorted(paymentComparator(sort))
                .toList();
    }

    private Comparator<Payment> paymentComparator(String sort) {
        Comparator<Payment> comparator = Comparator.comparing(Payment::getPaymentDate, Comparator.nullsLast(LocalDate::compareTo)).reversed();
        if ("dateAsc".equals(sort)) {
            comparator = Comparator.comparing(Payment::getPaymentDate, Comparator.nullsLast(LocalDate::compareTo));
        } else if ("amountAsc".equals(sort)) {
            comparator = Comparator.comparing(Payment::getAmount, Comparator.nullsLast(BigDecimal::compareTo));
        } else if ("amountDesc".equals(sort)) {
            comparator = Comparator.comparing(Payment::getAmount, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
        } else if ("employeeAsc".equals(sort)) {
            comparator = Comparator.comparing(this::employeeName, String.CASE_INSENSITIVE_ORDER);
        } else if ("employeeDesc".equals(sort)) {
            comparator = Comparator.comparing(this::employeeName, String.CASE_INSENSITIVE_ORDER).reversed();
        }
        return comparator;
    }

    private Map<Long, Map<String, String>> buildPaymentMeta(List<Payment> payments) {
        Map<Long, Map<String, String>> meta = new LinkedHashMap<>();
        for (Payment payment : payments) {
            Map<String, Object> info = payment.getPolicy() == null ? Map.of() : parseInfo(payment.getPolicy().getInfo());
            Map<String, String> values = new LinkedHashMap<>();
            values.put("employeeName", employeeName(payment));
            values.put("projectName", payment.getAssignment() == null ? "-" : payment.getAssignment().getProject().getProjectName());
            values.put("positionName", payment.getAssignment() == null ? "-" : payment.getAssignment().getPosition().getName());
            values.put("typeLabel", typeLabel(payment.getPolicy() == null ? null : payment.getPolicy().getPolicyType()));
            values.put("reason", firstNonBlank(text(info, "eventType"), text(info, "reason"), text(info, "description"), "-"));
            values.put("policyLabel", payment.getPolicy() == null ? "-" : payment.getPolicy().getPolicyType() + " #" + payment.getPolicy().getPolicyId());
            meta.put(payment.getPaymentId(), values);
        }
        return meta;
    }

    private Map<String, Object> parseInfo(String info) {
        if (info == null || info.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String employeeName(Payment payment) {
        return payment.getAssignment() == null ? "" : payment.getAssignment().getEmployee().getFullName();
    }

    private String typeLabel(PolicyType type) {
        if (type == null) {
            return "Без политики";
        }
        return switch (type) {
            case salary -> "Зарплата";
            case seniority -> "Бонус за стаж";
            case calendar_bonus -> "Календарная премия";
            case one_time_bonus -> "Разовый бонус";
        };
    }

    private String text(Map<String, Object> info, String key) {
        Object value = info.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private BigDecimal sum(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByType(List<Payment> payments, PolicyType type) {
        return payments.stream()
                .filter(payment -> payment.getPolicy() != null && payment.getPolicy().getPolicyType() == type)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal averageMonthly(BigDecimal total, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null || dateTo.isBefore(dateFrom)) {
            return total;
        }
        long months = ChronoUnit.MONTHS.between(dateFrom.withDayOfMonth(1), dateTo.withDayOfMonth(1)) + 1;
        return total.divide(BigDecimal.valueOf(Math.max(months, 1)), 2, RoundingMode.HALF_UP);
    }

    private String reportTitle(String type, Long employeeId) {
        if (employeeId != null) {
            return "Отчёт по расходам на сотрудника";
        }
        if (PolicyType.salary.name().equals(type)) {
            return "Ведомость по зарплате";
        }
        if (PolicyType.calendar_bonus.name().equals(type) || PolicyType.one_time_bonus.name().equals(type)) {
            return "Отчёт по премиям и бонусам";
        }
        if (PolicyType.seniority.name().equals(type)) {
            return "Отчёт по бонусам за стаж";
        }
        return "История начислений";
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
