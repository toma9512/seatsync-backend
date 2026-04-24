# ⚙ SeatSync Backend

> SeatSync 서비스의 백엔드 서버

Spring Boot 기반 REST API로 공연 조회, 좌석 선점, 예약 확정·취소 기능을 제공합니다.
Redis를 활용한 동시성 제어, QueryDSL 복합 필터 검색, AWS 배포 및 CI/CD 파이프라인을 직접 구성한 개인 프로젝트입니다.

> 💡 AI와의 **바이브 코딩(Vibe Coding)** 방식으로 설계부터 AWS 배포까지 **하루 만에** 완성했습니다.

🔗 [Frontend 레포](https://github.com/toma9512/seatsync-frontend)　|　🔗 [배포 주소](http://43.201.76.129)

---

## 🛠 Tech Stack

| 구분 | 기술 |
| --- | --- |
| Framework | Spring Boot |
| ORM | JPA · QueryDSL |
| Database | MySQL · Redis |
| 인증 | JWT · Spring Security |
| Infra | AWS (EC2, RDS) · Nginx · GitHub Actions |

---

## 📌 주요 기능

- JWT 인증 (회원가입 · 로그인)
- 공연 등록 · 조회 · 삭제
- QueryDSL 복합 필터 검색 (장르 · 지역 · 키워드)
- 회차 등록 · 조회
- 좌석 등록 · 조회
- Redis 기반 좌석 선점 (5분 TTL, 동시 선점 방지)
- 예약 확정 · 취소 · 내 예약 목록 조회
- Spring Scheduler 기반 만료 예약 자동 취소
- GitHub Actions + AWS EC2 CI/CD 자동 배포
- Nginx 리버스 프록시 설정

---

## ⭐ 핵심 기능 상세

### 🎟 좌석 선점 (Redis)

- `setIfAbsent(NX)` 원자적 연산으로 동시 요청 중 단 하나만 성공 보장
- 5분 TTL 설정으로 미확정 선점 자동 만료
- Spring Scheduler가 30초 주기로 만료된 PENDING 예약을 CANCELLED로 정리

```java
Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(key, email, 5, TimeUnit.MINUTES);

if (Boolean.FALSE.equals(success)) {
    throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
}
```

### 🔍 공연 검색 (QueryDSL)

- 장르 · 지역 · 키워드 조건이 선택적으로 조합되는 복합 필터 검색
- `BooleanExpression` null 반환 시 자동으로 조건에서 제외

```java
return queryFactory
        .selectFrom(event)
        .where(
                genreEq(genre),
                venueContains(venue),
                keywordContains(keyword)
        )
        .orderBy(event.createdAt.desc())
        .fetch();
```

---

## 🔑 기술적 의사결정

### 1. 좌석 선점 동시성 제어 — Redis setIfAbsent(NX) 선택

| 방식 | 검토 내용 |
| --- | --- |
| DB 비관적 락 | 트랜잭션 대기로 처리량 저하, 락 해제 타이밍 관리 복잡 |
| **Redis setIfAbsent(NX)** ✅ | 원자적 연산으로 단 하나의 요청만 성공 보장, TTL로 자동 만료 |

동일 좌석에 동시 요청이 들어왔을 때 DB 락은 대기 후 순차 처리되어 UX가 저하될 수 있습니다.
Redis NX 연산은 원자적으로 처리되어 첫 번째 요청만 성공하고 나머지는 즉시 실패 응답을 받아
사용자에게 빠른 피드백을 제공할 수 있습니다.

---

### 2. 복합 필터 검색 — QueryDSL 선택

| 방식 | 검토 내용 |
| --- | --- |
| MyBatis 동적 SQL | XML 작성 필요, 컴파일 타임 오류 감지 불가 |
| **QueryDSL** ✅ | 타입 안전한 동적 쿼리, BooleanExpression null 처리로 조건 자동 제외 |

장르 · 지역 · 키워드 조건이 선택적으로 조합되는 복합 필터 검색에서
QueryDSL의 `BooleanExpression`이 null 반환 시 자동으로 조건에서 제외되어
코드가 간결하고 타입 안전하게 유지됩니다.

---

### 3. MyBatis 대신 JPA 선택

| 방식 | 검토 내용 |
| --- | --- |
| MyBatis | SQL 직접 제어 가능, 복잡한 쿼리에 유리 |
| **JPA + QueryDSL** ✅ | 기본 CRUD 자동화, 복잡한 조건 검색은 QueryDSL로 보완 |

기존 팀 프로젝트(Route In)에서 MyBatis를 사용했기 때문에 이번 프로젝트에서는
JPA로 기본 CRUD를 자동화하고 QueryDSL로 복잡한 조건 검색을 처리하는 조합을 선택해
두 기술 모두 경험했습니다.

---

## 🔍 트러블슈팅

### 1. LazyInitializationException — fetch join으로 해결

**문제** : 예약 목록, 회차 목록 조회 시 연관 엔티티 접근 시 예외 발생

**원인** : JPA 지연 로딩(Lazy Loading) 설정으로 트랜잭션 종료 후 프록시 객체에 접근 시 세션 없어 초기화 불가

**해결** : Repository에 fetch join 쿼리 추가

```java
@Query("SELECT r FROM Reservation r JOIN FETCH r.seat JOIN FETCH r.user WHERE r.user.id = :userId")
List<Reservation> findByUserId(@Param("userId") Long userId);
```

**배운 점** : 연관 엔티티를 함께 조회해야 하는 경우 fetch join으로 한 번에 로딩해야
트랜잭션 범위 밖에서도 안전하게 접근 가능

---

### 2. Redis TTL 만료 후 DB 상태 미반영

**문제** : Redis TTL이 만료되어도 DB의 좌석 상태가 PENDING으로 유지

**원인** : Redis TTL 만료는 Redis에서만 처리되고 DB에는 반영되지 않음

**해결** : Spring Scheduler로 30초마다 `expiresAt`이 지난 PENDING 예약을 조회해
CANCELLED로 업데이트하고 좌석 상태를 AVAILABLE로 복구

```java
@Scheduled(fixedDelay = 30000)
@Transactional
public void cancelExpiredReservations() {
    List<Reservation> expired =
            reservationRepository.findExpiredPendingReservations(LocalDateTime.now());
    for (Reservation reservation : expired) {
        reservation.getSeat().updateStatus("AVAILABLE");
        reservation.updateStatus("CANCELLED");
    }
}
```

**배운 점** : Redis TTL과 DB 상태는 독립적으로 관리되므로 별도의 정합성 유지 로직이 필요함

---

### 3. CORS 에러

**문제** : 프론트(5173)에서 백엔드(8080) 요청 시 CORS 차단

**원인** : Spring Security 설정에 허용 Origin 미등록

**해결** : `SecurityConfig`에 `CorsConfigurationSource` 빈 등록,
배포 환경 Origin은 `application.yml` 환경변수로 분리해 코드에 노출 방지

---

## 🗄 Database

### 주요 테이블

| 테이블 | 설명 |
| --- | --- |
| users | 사용자 정보 · 인증 |
| events | 공연 정보 (제목 · 장르 · 장소) |
| schedules | 공연 회차 (날짜 · 잔여석) |
| seats | 좌석 정보 (등급 · 가격 · 상태) |
| reservations | 예약 내역 (선점 · 확정 · 취소) |

### 좌석 상태 흐름

```
AVAILABLE → (선점) → PENDING → (확정) → RESERVED
                              → (만료/취소) → AVAILABLE
```

---

## 🗂 API 명세

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | /api/auth/signup | 회원가입 | ❌ |
| POST | /api/auth/login | 로그인 | ❌ |
| GET | /api/events | 공연 목록 조회 (필터) | ✅ |
| POST | /api/events | 공연 등록 | ✅ |
| GET | /api/events/{id} | 공연 상세 조회 | ✅ |
| DELETE | /api/events/{id} | 공연 삭제 | ✅ |
| POST | /api/schedules | 회차 등록 | ✅ |
| GET | /api/schedules/events/{eventId} | 회차 목록 조회 | ✅ |
| POST | /api/seats | 좌석 등록 | ✅ |
| GET | /api/seats/schedules/{scheduleId} | 좌석 목록 조회 | ✅ |
| POST | /api/reservations/hold/{seatId} | 좌석 선점 | ✅ |
| POST | /api/reservations/confirm/{seatId} | 예약 확정 | ✅ |
| POST | /api/reservations/cancel/{reservationId} | 예약 취소 | ✅ |
| GET | /api/reservations/my | 내 예약 목록 | ✅ |
| GET | /api/reservations/remaining/{seatId} | 선점 잔여 시간 | ✅ |

---

## 🔗 링크

| 구분 | 링크 |
| --- | --- |
| 배포 (서비스) | http://43.201.76.129 |
| Backend 레포 | https://github.com/toma9512/seatsync-backend |
| Frontend 레포 | https://github.com/toma9512/seatsync-frontend |
| Notion | https://well-group-b12.notion.site/31f560d3713f8098b275d7074c98686e |
