package com.limeplus.fems.service;

import com.limeplus.fems.entity.PowerLog;
import com.limeplus.fems.modbus.ModbusClient;
import com.limeplus.fems.repository.PowerLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 수집 서비스: Modbus에서 읽어 DB에 저장하는 핵심 로직.
 * Python collector.py 의 while 루프에 해당한다.
 *
 * @Service : @Component 와 사실상 같지만, "비즈니스 로직 계층"임을 의미상 구분하는 어노테이션.
 *
 * 의존성 주입(DI):
 *   생성자에 ModbusClient, PowerLogRepository 를 받으면
 *   Spring이 알아서 해당 빈을 찾아 넣어준다. (직접 new 하지 않는다)
 *   → 이게 Spring의 핵심 개념. 객체 생성/연결을 프레임워크가 관리한다.
 */
@Service
public class CollectorService {

    private static final Logger log = LoggerFactory.getLogger(CollectorService.class);

    private final ModbusClient modbusClient;
    private final PowerLogRepository repository;

    public CollectorService(ModbusClient modbusClient, PowerLogRepository repository) {
        this.modbusClient = modbusClient;
        this.repository = repository;
    }

    /**
     * 1초(1000ms)마다 자동 실행.
     * @Scheduled(fixedRate = 1000) : 이전 실행 시작 시점 기준 1초마다 호출.
     *
     * Modbus 읽기가 실패해도 앱 전체가 죽으면 안 되므로 try-catch로 감싼다.
     * (현장에서 통신은 언제든 끊길 수 있다 — 운영 관점)
     */
    @Scheduled(fixedRate = 1000)
    public void collect() {
        try {
            ModbusClient.Reading r = modbusClient.read();
            PowerLog logEntry = new PowerLog(
                    OffsetDateTime.now(),
                    r.powerW(),
                    r.currentA(),
                    r.voltageV()
            );
            repository.save(logEntry);
            log.info("저장: {}W  {}A  {}V",
                    (int) r.powerW(), r.currentA(), r.voltageV());
        } catch (Exception e) {
            // 읽기 실패는 로그만 남기고 다음 주기에 재시도
            log.warn("Modbus 읽기 실패: {}", e.getMessage());
        }
    }
}
