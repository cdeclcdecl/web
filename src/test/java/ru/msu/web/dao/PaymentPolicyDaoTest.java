package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.PaymentPolicy;
import ru.msu.web.entity.PolicyType;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentPolicyDaoTest extends AbstractDaoTest {

    @Autowired
    private PaymentPolicyDao paymentPolicyDao;

    // findAll
    @Test
    void findAll_returnsAllPolicies() {
        List<PaymentPolicy> all = paymentPolicyDao.findAll();
        assertEquals(51, all.size());
    }

    // findByType
    @Test
    void findByType_salary() {
        List<PaymentPolicy> result = paymentPolicyDao.findByType(PolicyType.salary);
        assertEquals(37, result.size());
        assertTrue(result.stream().allMatch(PaymentPolicy::isFixed));
    }

    @Test
    void findByType_seniority() {
        List<PaymentPolicy> result = paymentPolicyDao.findByType(PolicyType.seniority);
        assertEquals(4, result.size());
        assertTrue(result.stream().noneMatch(PaymentPolicy::isFixed));
        assertTrue(result.stream().allMatch(p -> p.getInfo() != null && p.getInfo().contains("minYears")));
    }

    @Test
    void findByType_calendarBonus() {
        List<PaymentPolicy> result = paymentPolicyDao.findByType(PolicyType.calendar_bonus);
        assertEquals(5, result.size());
    }

    // findByAmountRange
    @Test
    void findByAmountRange_found() {
        List<PaymentPolicy> result = paymentPolicyDao.findByAmountRange(
                new BigDecimal("300000"), new BigDecimal("500000"));
        assertFalse(result.isEmpty());
    }

    @Test
    void findByAmountRange_notFound() {
        assertTrue(paymentPolicyDao.findByAmountRange(
                new BigDecimal("999999"), new BigDecimal("9999999")).isEmpty());
    }

    // findById
    @Test
    void findById_found() {
        PaymentPolicy p = paymentPolicyDao.findById(1L);
        assertNotNull(p);
        assertEquals(PolicyType.salary, p.getPolicyType());
        assertTrue(p.isFixed());
        assertTrue(p.getInfo().contains("projectId"));
    }

    @Test
    void findById_notFound() {
        assertNull(paymentPolicyDao.findById(9999L));
    }

    // save
    @Test
    void save_createsNewPolicy() {
        PaymentPolicy pp = new PaymentPolicy();
        pp.setPolicyType(PolicyType.one_time_bonus);
        pp.setFixed(true);
        pp.setAmount(new BigDecimal("60000.00"));
        pp.setInfo("{\"reason\": \"Тестовый бонус\"}");
        PaymentPolicy saved = paymentPolicyDao.save(pp);
        assertNotNull(saved.getPolicyId());

        PaymentPolicy found = paymentPolicyDao.findById(saved.getPolicyId());
        assertNotNull(found);
        assertEquals(PolicyType.one_time_bonus, found.getPolicyType());
        assertTrue(found.getInfo().contains("Тестовый бонус"));
    }
}
