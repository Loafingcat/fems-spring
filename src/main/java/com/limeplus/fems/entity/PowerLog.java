package com.limeplus.fems.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "power_log")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PowerLog {

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

    public PowerLog(OffsetDateTime ts, double powerW, double currentA, double voltageV) {
        this.ts = ts;
        this.powerW = powerW;
        this.currentA = currentA;
        this.voltageV = voltageV;
    }
}