package com.limeplus.fems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 애플리케이션 진입점.
 *
 * @SpringBootApplication : 아래 3개를 합친 어노테이션
 *   - @Configuration       : 설정 클래스임을 표시
 *   - @EnableAutoConfiguration : classpath에 있는 라이브러리 보고 자동 설정 (DB, 웹서버 등)
 *   - @ComponentScan       : 이 패키지 하위의 @Component/@Service/@Controller를 자동 등록
 *
 * @EnableScheduling : @Scheduled 붙은 메서드를 주기적으로 실행하게 함 (Modbus 폴링에 사용)
 */
@SpringBootApplication
@EnableScheduling
public class FemsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FemsApplication.class, args);
    }
}
