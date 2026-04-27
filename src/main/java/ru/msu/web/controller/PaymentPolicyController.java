package ru.msu.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.web.dao.PaymentPolicyDao;
import ru.msu.web.dao.PositionDao;
import ru.msu.web.dao.ProjectDao;
import ru.msu.web.entity.PaymentPolicy;
import ru.msu.web.entity.PolicyType;
import ru.msu.web.entity.Position;
import ru.msu.web.entity.Project;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/policies")
public class PaymentPolicyController {

    private final PaymentPolicyDao paymentPolicyDao;
    private final ProjectDao projectDao;
    private final PositionDao positionDao;
    private final ObjectMapper objectMapper;

    public PaymentPolicyController(PaymentPolicyDao paymentPolicyDao, ProjectDao projectDao,
                                   PositionDao positionDao, ObjectMapper objectMapper) {
        this.paymentPolicyDao = paymentPolicyDao;
        this.projectDao = projectDao;
        this.positionDao = positionDao;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String type,
                       @RequestParam(required = false) BigDecimal amountMin,
                       @RequestParam(required = false) BigDecimal amountMax,
                       @RequestParam(required = false) Long projectId,
                       @RequestParam(required = false) Long positionId,
                       @RequestParam(required = false) Integer minYears,
                       @RequestParam(required = false) String event,
                       @RequestParam(required = false, defaultValue = "amountDesc") String sort,
                       Model model) {
        List<PaymentPolicy> source = paymentPolicyDao.findAll();
        List<PaymentPolicy> policies = source.stream()
                .filter(policy -> type == null || type.isBlank() || policy.getPolicyType().name().equals(type))
                .filter(policy -> amountMin == null || policy.getAmount().compareTo(amountMin) >= 0)
                .filter(policy -> amountMax == null || policy.getAmount().compareTo(amountMax) <= 0)
                .filter(policy -> projectId == null || Objects.equals(longValue(parseInfo(policy.getInfo()), "projectId"), projectId))
                .filter(policy -> positionId == null || Objects.equals(longValue(parseInfo(policy.getInfo()), "positionId"), positionId))
                .filter(policy -> minYears == null || intValue(parseInfo(policy.getInfo()), "minYears") != null && intValue(parseInfo(policy.getInfo()), "minYears") >= minYears)
                .filter(policy -> event == null || event.isBlank() || containsMeta(parseInfo(policy.getInfo()), event))
                .sorted(policyComparator(sort))
                .toList();
        model.addAttribute("policies", policies);
        model.addAttribute("policyMeta", buildPolicyMeta(policies));
        model.addAttribute("policyTypes", PolicyType.values());
        model.addAttribute("projects", projectDao.findAll());
        model.addAttribute("positions", positionDao.findAll());
        model.addAttribute("type", type);
        model.addAttribute("amountMin", amountMin);
        model.addAttribute("amountMax", amountMax);
        model.addAttribute("projectId", projectId);
        model.addAttribute("positionId", positionId);
        model.addAttribute("minYears", minYears);
        model.addAttribute("event", event);
        model.addAttribute("sort", sort);
        model.addAttribute("missingProjects", missingProjects(source));
        model.addAttribute("missingPositions", missingPositions(source));
        return "policies/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        PaymentPolicy policy = new PaymentPolicy();
        policy.setFixed(true);
        populateForm(model, policy);
        return "policies/form";
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PaymentPolicy policy = paymentPolicyDao.findById(id);
        if (policy == null) {
            return "redirect:/policies";
        }
        populateForm(model, policy);
        return "policies/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String policyType,
                         @RequestParam(defaultValue = "false") boolean fixed,
                         @RequestParam String amount,
                         @RequestParam(required = false) Long projectId,
                         @RequestParam(required = false) Long positionId,
                         @RequestParam(required = false) Integer minYears,
                         @RequestParam(required = false) String eventType,
                         @RequestParam(required = false) String reason,
                         @RequestParam(required = false) String description,
                         Model model) {
        return savePolicy(new PaymentPolicy(), policyType, fixed, amount, projectId, positionId, minYears, eventType, reason, description, model);
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String policyType,
                         @RequestParam(defaultValue = "false") boolean fixed,
                         @RequestParam String amount,
                         @RequestParam(required = false) Long projectId,
                         @RequestParam(required = false) Long positionId,
                         @RequestParam(required = false) Integer minYears,
                         @RequestParam(required = false) String eventType,
                         @RequestParam(required = false) String reason,
                         @RequestParam(required = false) String description,
                         Model model) {
        PaymentPolicy policy = paymentPolicyDao.findById(id);
        if (policy == null) {
            return "redirect:/policies";
        }
        return savePolicy(policy, policyType, fixed, amount, projectId, positionId, minYears, eventType, reason, description, model);
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        PaymentPolicy policy = paymentPolicyDao.findById(id);
        if (policy == null) {
            return "redirect:/policies";
        }
        try {
            paymentPolicyDao.delete(policy);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Политику нельзя удалить, пока она используется в начислениях");
        }
        return "redirect:/policies";
    }

    private String savePolicy(PaymentPolicy policy, String policyType, boolean fixed, String amount,
                              Long projectId, Long positionId, Integer minYears,
                              String eventType, String reason, String description,
                              Model model) {
        if (amount == null || amount.isBlank()) {
            model.addAttribute("error", "Сумма обязательна для заполнения");
            populateForm(model, policy);
            return "policies/form";
        }
        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Некорректная сумма политики");
            populateForm(model, policy);
            return "policies/form";
        }
        policy.setPolicyType(PolicyType.valueOf(policyType));
        policy.setFixed(fixed);
        policy.setAmount(parsedAmount);
        policy.setInfo(buildInfo(projectId, positionId, minYears, eventType, reason, description));
        paymentPolicyDao.save(policy);
        return "redirect:/policies";
    }

    private void populateForm(Model model, PaymentPolicy policy) {
        Map<String, Object> info = parseInfo(policy.getInfo());
        model.addAttribute("policy", policy);
        model.addAttribute("policyTypes", PolicyType.values());
        model.addAttribute("projects", projectDao.findAll());
        model.addAttribute("positions", positionDao.findAll());
        model.addAttribute("selectedProjectId", longValue(info, "projectId"));
        model.addAttribute("selectedPositionId", longValue(info, "positionId"));
        model.addAttribute("selectedMinYears", intValue(info, "minYears"));
        model.addAttribute("selectedEventType", text(info, "eventType"));
        model.addAttribute("selectedReason", text(info, "reason"));
        model.addAttribute("selectedDescription", text(info, "description"));
        model.addAttribute("rawInfo", policy.getInfo());
    }

    private Map<Long, Map<String, String>> buildPolicyMeta(List<PaymentPolicy> policies) {
        Map<Long, Map<String, String>> meta = new LinkedHashMap<>();
        for (PaymentPolicy policy : policies) {
            Map<String, Object> info = parseInfo(policy.getInfo());
            Map<String, String> values = new LinkedHashMap<>();
            values.put("projectName", projectName(longValue(info, "projectId")));
            values.put("positionName", positionName(longValue(info, "positionId")));
            values.put("minYears", text(info, "minYears"));
            values.put("event", firstNonBlank(text(info, "eventType"), text(info, "reason")));
            values.put("description", text(info, "description"));
            values.put("info", policy.getInfo() == null ? "-" : policy.getInfo());
            meta.put(policy.getPolicyId(), values);
        }
        return meta;
    }

    private Comparator<PaymentPolicy> policyComparator(String sort) {
        Comparator<PaymentPolicy> comparator = Comparator.comparing(PaymentPolicy::getAmount, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
        if ("amountAsc".equals(sort)) {
            comparator = Comparator.comparing(PaymentPolicy::getAmount, Comparator.nullsLast(BigDecimal::compareTo));
        } else if ("minYearsAsc".equals(sort)) {
            comparator = Comparator.comparing(policy -> intValue(parseInfo(policy.getInfo()), "minYears"), Comparator.nullsLast(Integer::compareTo));
        } else if ("minYearsDesc".equals(sort)) {
            comparator = Comparator.comparing((PaymentPolicy policy) -> intValue(parseInfo(policy.getInfo()), "minYears"), Comparator.nullsLast(Integer::compareTo)).reversed();
        }
        return comparator;
    }

    private List<Project> missingProjects(List<PaymentPolicy> policies) {
        List<Long> covered = policies.stream()
                .filter(policy -> policy.getPolicyType() == PolicyType.salary)
                .map(policy -> longValue(parseInfo(policy.getInfo()), "projectId"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return projectDao.findAll().stream()
                .filter(project -> !covered.contains(project.getProjectId()))
                .toList();
    }

    private List<Position> missingPositions(List<PaymentPolicy> policies) {
        List<Long> covered = policies.stream()
                .filter(policy -> policy.getPolicyType() == PolicyType.salary)
                .map(policy -> longValue(parseInfo(policy.getInfo()), "positionId"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return positionDao.findAll().stream()
                .filter(position -> !covered.contains(position.getPositionId()))
                .toList();
    }

    private String buildInfo(Long projectId, Long positionId, Integer minYears,
                             String eventType, String reason, String description) {
        Map<String, Object> info = new LinkedHashMap<>();
        if (projectId != null) {
            info.put("projectId", projectId);
        }
        if (positionId != null) {
            info.put("positionId", positionId);
        }
        if (minYears != null) {
            info.put("minYears", minYears);
        }
        if (eventType != null && !eventType.isBlank()) {
            info.put("eventType", eventType);
        }
        if (reason != null && !reason.isBlank()) {
            info.put("reason", reason);
        }
        if (description != null && !description.isBlank()) {
            info.put("description", description);
        }
        if (info.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return null;
        }
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

    private boolean containsMeta(Map<String, Object> info, String value) {
        String query = value.toLowerCase();
        return info.values().stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::toLowerCase)
                .anyMatch(text -> text.contains(query));
    }

    private Long longValue(Map<String, Object> info, String key) {
        Object value = info.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private Integer intValue(Map<String, Object> info, String key) {
        Object value = info.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return null;
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

    private String projectName(Long projectId) {
        if (projectId == null) {
            return "-";
        }
        Project project = projectDao.findById(projectId);
        return project == null ? "-" : project.getProjectName();
    }

    private String positionName(Long positionId) {
        if (positionId == null) {
            return "-";
        }
        Position position = positionDao.findById(positionId);
        return position == null ? "-" : position.getName();
    }
}
