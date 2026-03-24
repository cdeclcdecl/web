package ru.msu.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.msu.web.entity.PaymentPolicy;
import ru.msu.web.entity.PolicyType;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentPolicyRepository extends JpaRepository<PaymentPolicy, Long> {

    List<PaymentPolicy> findByPolicyType(PolicyType type);

    List<PaymentPolicy> findByFixed(boolean fixed);

    List<PaymentPolicy> findByAmountBetween(BigDecimal min, BigDecimal max);
}
