package com.limeplus.fems.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * 전력 측정값 한 건을 나타내는 Entity.
 * JPA가 이 클래스를 power_log 테이블의 한 행(row)과 매핑한다.
 *
 * @Entity : 이 클래스가 DB 테이블과 매핑되는 객체임을 표시
 * @Table  : 매핑할 테이블 이름 지정 (생략하면 클래스명 기반으로 자동 생성)
 */
@Entity
@Table(name = "power_log")
public class PowerLog {

    /**
     * 기본키. IDENTITY 전략 = DB가 자동 증가(serial/auto increment)로 값을 매김.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime ts;

    @Column(name = "power_w", nullable = false)
    private double powerW;

    @Column(name = "current_a")
    private double currentA;

    @Column(name = "voltage_v")
    private double voltageV;

    // JPA는 기본 생성자(no-args)가 반드시 필요하다. (리플렉션으로 객체를 만들기 때문)
    protected PowerLog() {
    }

    // 실제 코드에서 값을 채워 만들 때 쓰는 생성자
    public PowerLog(OffsetDateTime ts, double powerW, double currentA, double voltageV) {
        this.ts = ts;
        this.powerW = powerW;
        this.currentA = currentA;
        this.voltageV = voltageV;
    }

    // --- getter (REST 응답으로 JSON 직렬화할 때 필요) ---
    public Long getId() { return id; }
    public OffsetDateTime getTs() { return ts; }
    public double getPowerW() { return powerW; }
    public double getCurrentA() { return currentA; }
    public double getVoltageV() { return voltageV; }
}
