package com.limeplus.fems.repository;

import com.limeplus.fems.entity.PowerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 데이터 접근 계층(DAO).
 *
 * JpaRepository<PowerLog, Long> 을 상속하면
 * save(), findAll(), findById(), delete() 같은 기본 CRUD 메서드가 공짜로 생긴다.
 * 구현 클래스를 우리가 안 짜도 Spring Data JPA가 런타임에 자동으로 만들어준다.
 *
 * 아래 두 메서드는 "쿼리 메서드" 기능:
 *   메서드 이름 규칙만 맞추면 Spring이 SQL을 자동 생성한다.
 */
public interface PowerLogRepository extends JpaRepository<PowerLog, Long> {

    /**
     * 가장 최근 측정값 1건.
     * findTopByOrderByTsDesc =
     *   "Ts 내림차순 정렬 후 맨 위(Top) 1건을 찾아라"
     * → SELECT * FROM power_log ORDER BY ts DESC LIMIT 1
     */
    PowerLog findTopByOrderByTsDesc();

    /**
     * 특정 시각 이후의 데이터를 시간순으로.
     * 직접 JPQL을 쓰는 방식(@Query). 복잡한 쿼리는 이렇게 명시한다.
     */
    @Query("SELECT p FROM PowerLog p WHERE p.ts > ?1 ORDER BY p.ts ASC")
    List<PowerLog> findRecent(OffsetDateTime since);
}
