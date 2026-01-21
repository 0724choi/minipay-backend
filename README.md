# Mini Payment Transaction System

등록–수납 처리 흐름을 단순화하여 구현한 **미니 금융 처리 백엔드** 프로젝트입니다.  
금융권 SI 환경을 가정하여 **금액 및 상태 변경 시 데이터 정합성**과 **트랜잭션 안정성**을 중점적으로 설계했습니다.

---

## Key Points
- Spring Boot + JPA 기반 백엔드
- Oracle DB 사용 (Docker 기반 Oracle 21c XE 환경)
- **PL/SQL 프로시저 기반** 금액·상태 변경 처리
- Spring `@Transactional` 과 **DB 트랜잭션(DB-managed)** 비교·활용
- 상태 전이 기반 수납 흐름 관리 (**UNPAID → PAID → CANCELED**)

---

## Purpose
금융권 SI 환경에서 요구되는 **트랜잭션 안정성**과 **데이터 정합성**을  
**Spring 트랜잭션(애플리케이션 레벨)** 과 **DB 트랜잭션(프로시저 레벨)** 관점에서 직접 비교·정리하기 위한 개인 프로젝트입니다.

---

## Architecture
본 프로젝트는 조회(Read)와 상태·금액 변경(Write)의 책임을 분리하여,  
트랜잭션 경계를 명확히 하고 책임 주체를 드러내도록 설계했습니다.

- 흐름: `Controller → Service → Repository(JPA) / Procedure Repository`
- 조회 로직은 **JPA 기반**으로 처리
- 금액·상태 변경 로직은 **Oracle PL/SQL 프로시저**로 분리
- 트랜잭션 경계를 명확히 하여 책임 주체를 구분

---

## Transaction Strategy
본 프로젝트에서는 트랜잭션 처리 방식을 두 가지 관점에서 구현하여 비교합니다.

### 1) Application-managed Transaction
- Spring `@Transactional`을 사용하여 트랜잭션을 제어
- PL/SQL 프로시저는 **COMMIT/ROLLBACK 없이** 로직 수행만 담당
- 애플리케이션 레벨에서 트랜잭션 경계 관리

### 2) DB-managed Transaction
- Oracle PL/SQL 프로시저 내부에서 **COMMIT/ROLLBACK을 직접 수행**
- 여러 테이블 변경을 DB 레벨 트랜잭션으로 묶어 원자성과 정합성 보장
- 애플리케이션 장애 상황에서도 데이터 일관성 유지 가능하도록 설계

---

## Status Flow
`UNPAID → PAID → CANCELED`

- **UNPAID** 상태에서만 수납 가능
- **PAID** 상태에서만 취소 가능
- 상태 전이 규칙을 통해 중복 수납 및 비정상 처리 방지

---

## Notes
- 본 프로젝트는 UI 없이 **API 중심**으로 구성되며, Swagger를 통해 요청/응답 및 상태 변화를 확인합니다.
- 실제 금융권 SI 환경(온프레미스, DB 중심 구조)을 고려한 설계를 목표로 합니다.
