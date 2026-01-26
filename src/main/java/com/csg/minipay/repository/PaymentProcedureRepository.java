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

    // DB-managed PAY (PAYMENT_TXN_PKG.PAY_DB_MANAGED) 호출
    public ProcResult payDbManaged(Long paymentId, String requestId, Long amount) {

        StoredProcedureQuery query =
                em.createStoredProcedureQuery("PAYMENT_TXN_PKG.PAY_DB_MANAGED");

        // ⚠️ 이름 매칭 이슈 대비: 순서 기반 등록이 오라클에서 더 안전함
        query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);     // p_payment_id
        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);   // p_request_id
        query.registerStoredProcedureParameter(3, Long.class, ParameterMode.IN);     // p_amount
        query.registerStoredProcedureParameter(4, String.class, ParameterMode.OUT); // o_result_code
        query.registerStoredProcedureParameter(5, String.class, ParameterMode.OUT); // o_result_msg

        query.setParameter(1, paymentId);
        query.setParameter(2, requestId);
        query.setParameter(3, amount);

        query.execute();

        return new ProcResult(
                (String) query.getOutputParameterValue(4),
                (String) query.getOutputParameterValue(5)
        );
    }
}
