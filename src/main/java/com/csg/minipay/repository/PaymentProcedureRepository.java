package com.csg.minipay.repository;

import com.csg.minipay.dto.ProcResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentProcedureRepository {

    private final EntityManager em;

    public ProcResult payApp(Long paymentId, String requestId, Long amount) {

        StoredProcedureQuery query =
                em.createStoredProcedureQuery("SP_PAY_APP");

        query.registerStoredProcedureParameter("p_payment_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_request_id", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_amount", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("o_code", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("o_msg", String.class, ParameterMode.OUT);

        query.setParameter("p_payment_id", paymentId);
        query.setParameter("p_request_id", requestId);
        query.setParameter("p_amount", amount);

        query.execute();

        return new ProcResult(
                (String) query.getOutputParameterValue("o_code"),
                (String) query.getOutputParameterValue("o_msg")
        );
    }
}
