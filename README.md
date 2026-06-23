# FEMS 전력 모니터링 (Spring Boot)

Modbus TCP로 전력계 데이터를 수집해 PostgreSQL에 저장하고 대시보드로 보여주는 시스템을,
Java/Spring Boot로 구현한 버전입니다. 

산업용 통신 프로토콜(Modbus)과 기업용 백엔드 프레임워크(Spring Boot)를 함께 다뤄보는 것이 목적입니다.

## 전체 구조

```
Python 시뮬레이터 (전력계 역할, 그대로 둠)
        │  Modbus TCP (포트 5020)
        ▼
┌─────────────────────────────────────────┐
│            Spring Boot 앱                 │
│                                           │
│  ModbusClient   ──폴링──▶  CollectorService │
│  (j2mod)                   (@Scheduled 1초) │
│                                  │         │
│                                  ▼         │
│                          PowerLogRepository │
│                          (JPA, 자동 구현)    │
│                                  │         │
│                                  ▼         │
│  PowerController ◀──조회──   PostgreSQL     │
│  (REST API, 8000포트)                       │
└─────────────────────────────────────────┘
        │
        ▼
   dashboard.html (브라우저)
```

---

## Spring Boot 핵심 개념 (가물가물할 때 다시 보기)

### 1. 의존성 주입(DI)과 빈(Bean)

Spring의 가장 중요한 개념. **객체를 직접 `new` 하지 않고, Spring이 만들어서 넣어준다.**

```java
@Service
public class CollectorService {
    private final ModbusClient modbusClient;       // 직접 new 안 함
    private final PowerLogRepository repository;

    public CollectorService(ModbusClient modbusClient, PowerLogRepository repository) {
        this.modbusClient = modbusClient;          // Spring이 생성자로 넣어줌
        this.repository = repository;
    }
}
```

- `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController` 가 붙은 클래스는
  Spring이 시작할 때 **객체를 하나 만들어 보관**한다. 이걸 "빈(bean)"이라고 부른다.
- 다른 클래스가 생성자에서 그 타입을 요구하면, Spring이 보관해둔 빈을 찾아 **자동으로 주입**한다.
- 그래서 우리는 `new ModbusClient()` 를 어디에도 안 쓴다. 객체 생성·연결을 프레임워크가 관리한다.
- 이게 왜 좋은가? 객체 간 결합이 느슨해져 테스트·교체가 쉬워진다.
  (예: Modbus 대신 가짜 클라이언트를 주입해 테스트 가능)

### 2. 어노테이션이 곧 설정

Python은 코드를 직접 호출하지만, Spring은 **어노테이션을 보고 동작을 결정**한다.

| 어노테이션 | 역할 |
|-----------|------|
| `@SpringBootApplication` | 앱 진입점. 자동 설정 + 컴포넌트 스캔을 켠다 |
| `@EnableScheduling` | `@Scheduled` 메서드를 주기 실행하게 함 |
| `@Component` / `@Service` | 이 클래스를 빈으로 등록 |
| `@Entity` | 이 클래스를 DB 테이블과 매핑 |
| `@RestController` | REST API 컨트롤러. 반환값을 JSON으로 변환 |
| `@Scheduled(fixedRate=1000)` | 1초마다 이 메서드 실행 |
| `@GetMapping("/latest")` | GET /latest 요청을 이 메서드로 연결 |

### 3. JPA — DB를 자바 객체로

JPA(+Hibernate)는 SQL을 직접 안 짜도 되게 해주는 ORM이다.

- **Entity** (`PowerLog`): 테이블의 한 행 = 자바 객체 하나. 필드가 컬럼에 매핑된다.
- **Repository** (`PowerLogRepository`): `JpaRepository`를 상속한 **인터페이스만** 만들면
  `save()`, `findAll()` 같은 메서드를 Spring이 **런타임에 자동 구현**한다. 우리가 구현 클래스를 안 짠다.
- **쿼리 메서드**: `findTopByOrderByTsDesc()` 처럼 메서드 이름 규칙만 맞추면 SQL이 자동 생성된다.
  - `findTop...` = LIMIT 1
  - `...ByOrderByTsDesc` = ORDER BY ts DESC
  - 복잡한 건 `@Query`로 JPQL을 직접 쓴다.

---

## 파일별 역할

| 파일 | 역할 | Python 대응 |
|------|------|-------------|
| `pom.xml` | 의존성·빌드 정의 | requirements.txt |
| `FemsApplication.java` | 앱 진입점 | `if __name__ == "__main__"` |
| `entity/PowerLog.java` | 테이블 ↔ 객체 매핑 | (SQL 스키마) |
| `repository/PowerLogRepository.java` | DB 접근 (자동 구현) | psycopg2 쿼리 |
| `modbus/ModbusClient.java` | 전력계 Modbus 읽기 | pymodbus 클라이언트 |
| `service/CollectorService.java` | 1초마다 읽어 저장 | collector.py while 루프 |
| `controller/PowerController.java` | REST API | FastAPI `@app.get` |
| `application.properties` | DB·서버 설정 | DB_DSN 문자열 |

---

## Python 버전과의 차이 (개념 비교)

| | Python | Spring Boot |
|---|--------|-------------|
| Modbus | pymodbus (async) | j2mod (동기) |
| 주기 실행 | `while True: sleep(1)` | `@Scheduled(fixedRate=1000)` |
| DB 접근 | psycopg2로 SQL 직접 | JPA Repository (자동) |
| API | FastAPI 함수 | `@RestController` 메서드 |
| 객체 생성 | 직접 생성/연결 | Spring이 주입(DI) |
| 설정 | 코드 안 상수 | application.properties |

가장 큰 사고방식 차이: **Python은 "내가 흐름을 짠다", Spring은 "프레임워크가 흐름을 관리하고 나는 빈 칸을 채운다"** (제어의 역전, IoC).

---

## 실행 방법

```bash
# 1. PostgreSQL (Python 버전과 동일하게 docker로)
docker run -d --name fems-db \
  -e POSTGRES_DB=fems -e POSTGRES_USER=fems -e POSTGRES_PASSWORD=fems \
  -p 5432:5432 postgres:16
# 테이블은 JPA가 자동 생성하므로 schema.sql 안 돌려도 됨 (ddl-auto=update)

# 2. Modbus 전력계 시뮬레이터 (Python, 데이터 소스)
pip install pymodbus==3.6.9
python simulator/power_meter.py

# 3. Spring Boot 앱 실행 (의존성 자동 다운로드 + 빌드 + 실행)
mvn spring-boot:run

# 4. dashboard.html 을 브라우저로 열기 (API가 8000포트)
```

콘솔에 `저장: 21000W  95.5A  220.3V` 가 1초마다 찍히고,
`spring.jpa.show-sql=true` 덕분에 실행되는 INSERT SQL도 함께 보인다.

---

## 학습 순서 (이대로 따라가며 손에 익히기)

1. **`mvn spring-boot:run` 으로 일단 돌려본다.** 콘솔 로그와 SQL을 눈으로 확인.
2. **`PowerController`부터 읽는다.** REST가 제일 직관적. `/api/latest`를 브라우저로 직접 호출해보기.
3. **`CollectorService`의 DI를 이해한다.** 생성자에 받은 객체를 누가 넣어주는지(Spring) 짚기.
4. **`PowerLogRepository`의 자동 구현이 신기한 지점.** 인터페이스만 있는데 어떻게 동작하는지 검색.
5. **직접 변형해보기 (가장 중요):**
   - 폴링 주기를 2초로 바꿔보기 (`fixedRate = 2000`)
   - `/api/stats` 같은 새 엔드포인트를 직접 추가해보기
   - 전류·전압 외에 다른 값을 추가해보기 (시뮬레이터 레지스터도 같이 수정)

> ⚠️ 이 코드는 학습 출발점입니다. **한 줄씩 이해하고 직접 다시 타이핑하면서** 본인 것으로 만드세요.
> 면접에서 "이 어노테이션 왜 붙였나요?"에 답할 수 있어야 진짜 실력입니다.

---

# 실행 방법

DB · 시뮬레이터 · Spring 앱 세 개가 모두 떠 있어야 대시보드에 데이터가 나옵니다.
터미널 2개 + 브라우저 1개를 사용합니다.

## 사전 준비 (최초 1회)

```bash
# DB 컨테이너 처음 생성
docker run -d --name fems-db \
  -e POSTGRES_DB=fems -e POSTGRES_USER=fems -e POSTGRES_PASSWORD=fems \
  -p 5432:5432 postgres:16

# 이미 만들어둔 경우엔 start만
docker start fems-db
```

> 테이블(power_log)은 JPA가 자동 생성하므로 별도 스키마 실행은 불필요합니다. (`ddl-auto=update`)

## 터미널 1 — Modbus 전력계 시뮬레이터 (데이터 소스)

```bash
cd /mnt/c/Users/금정산2-PC12/Downloads/fems-spring/fems-spring
source .venv/bin/activate          # 최초엔: uv venv && source .venv/bin/activate && uv pip install pymodbus==3.6.9
python simulator/power_meter.py
```

`Modbus 전력계 시뮬레이터 시작 (포트 5020)` 이 뜨면 이 터미널은 그대로 둡니다.

## 터미널 2 — Spring Boot 앱

```bash
cd /mnt/c/Users/금정산2-PC12/Downloads/fems-spring/fems-spring
mvn spring-boot:run
```

`Started FemsApplication` 이 뜨고, 1초마다 `저장: 13634W  61.4A  221.7V` 같은 로그가 나오면 정상입니다.

## 브라우저 — 대시보드

```
http://localhost:8000
```

전력·전류·전압 카드와 차트가 3초마다 갱신됩니다.

## 종료

```bash
# 각 터미널에서 Ctrl + C
docker stop fems-db
```

---

## 트러블슈팅 (직접 겪은 것들)

- **`release version 21 not supported`** : Maven이 쓰는 JDK가 21보다 낮음.
  `sudo apt install -y openjdk-21-jdk` 후 `sudo update-alternatives --config javac` 로 21 선택.
  (또는 `pom.xml`의 `<java.version>`을 설치된 버전에 맞춰 17로 낮춤 — Spring Boot 3.3은 17부터 지원)
- **`pip/python not found`** : WSL에 파이썬 미설치. `uv venv` + `uv pip install` 로 가상환경 사용.
- **대시보드에 값이 안 들어옴 (`-- W`)** : API는 정상인데 화면만 안 바뀌는 경우, JSON 키 네이밍 불일치.
  자바(Jackson)는 `powerW`(camelCase), 대시보드는 `power_w`(snake_case)를 기대.
  `application.properties`에 `spring.jackson.property-naming-strategy=SNAKE_CASE` 추가로 해결.
- **대시보드가 옛 화면** : `file://`이 아니라 반드시 `http://localhost:8000` 으로 접속, `Ctrl+Shift+R` 강력 새로고침.
```

## 아직 안 한 것 / 다음 과제

- [ ] **MQTT 추가**: Eclipse Paho Java client로 센서 데이터 구독 (Modbus는 폴링, MQTT는 이벤트)
- [ ] **DB 재연결 처리**: 지금은 Modbus 끊기면 로그만 남기고 다음 주기 재시도. 더 견고하게.
- [ ] **시계열 최적화**: PostgreSQL을 TimescaleDB로 바꿔 하이퍼테이블·연속집계 적용
- [ ] **테스트 코드**: 가짜 ModbusClient를 주입해 CollectorService 단위 테스트 (DI의 장점 활용)
