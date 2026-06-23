package com.limeplus.fems.controller;

import com.limeplus.fems.entity.PowerLog;
import com.limeplus.fems.repository.PowerLogRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST API 컨트롤러. 대시보드(브라우저)가 호출하는 엔드포인트.
 * Python FastAPI 의 @app.get(...) 에 해당한다.
 *
 * @RestController : @Controller + @ResponseBody.
 *     반환한 객체를 Spring이 자동으로 JSON으로 변환해 응답 본문에 담는다.
 * @RequestMapping("/api") : 이 컨트롤러의 모든 경로 앞에 /api 가 붙는다.
 * @CrossOrigin : 브라우저(다른 출처)에서 fetch 호출을 허용 (CORS).
 *     실무에선 allowedOrigins 를 특정 도메인으로 좁히는 게 안전하지만,
 *     로컬 대시보드 테스트용으로 전체 허용.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PowerController {

    private final PowerLogRepository repository;

    public PowerController(PowerLogRepository repository) {
        this.repository = repository;
    }

    /**
     * GET /api/latest : 가장 최근 측정값 1건.
     */
    @GetMapping("/latest")
    public PowerLog latest() {
        return repository.findTopByOrderByTsDesc();
    }

    /**
     * GET /api/recent : 최근 30분 데이터 (차트용).
     */
    @GetMapping("/recent")
    public List<PowerLog> recent() {
        OffsetDateTime since = OffsetDateTime.now().minusMinutes(30);
        return repository.findRecent(since);
    }
}
