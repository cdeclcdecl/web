package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.PaymentPolicy;
import ru.msu.web.entity.PolicyType;
import ru.msu.web.repository.PaymentPolicyRepository;

import java.math.BigDecimal;
import java.util.List;

/*
 * DAO для работы с политиками выплат
 * Поддерживает операции страницы "Политики выплат"
 */
@Service
public class PaymentPolicyDao {

    private final PaymentPolicyRepository paymentPolicyRepository;

    public PaymentPolicyDao(PaymentPolicyRepository paymentPolicyRepository) {
        this.paymentPolicyRepository = paymentPolicyRepository;
    }

    /* Все политики */
    public List<PaymentPolicy> findAll() {
        return paymentPolicyRepository.findAll();
    }

    /* Политики по типу */
    public List<PaymentPolicy> findByType(PolicyType type) {
        return paymentPolicyRepository.findByPolicyType(type);
    }

    /* Политики по диапазону суммы */
    public List<PaymentPolicy> findByAmountRange(BigDecimal min, BigDecimal max) {
        return paymentPolicyRepository.findByAmountBetween(min, max);
    }

    /* Получить политику по ID. Возвращает null, если не найдена. */
    public PaymentPolicy findById(Long id) {
        return paymentPolicyRepository.findById(id).orElse(null);
    }

    /* Сохранить (создать или обновить) политику */
    public PaymentPolicy save(PaymentPolicy policy) {
        return paymentPolicyRepository.save(policy);
    }

    public void delete(PaymentPolicy policy) {
        paymentPolicyRepository.delete(policy);
    }
}
