package nus.edu.u.task.controller;

import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/sentinel-test")
@Slf4j
public class SentinelTestController {

    private final Random random = new Random();
    private final AtomicLong requestCounter = new AtomicLong(0);

    /**
     * 测试1: 简单接口
     */
    @GetMapping("/simple")
    public CommonResult<Map<String, Object>> simple() {
        long count = requestCounter.incrementAndGet();
        log.info("Simple request #{}", count);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", count);
        data.put("timestamp", System.currentTimeMillis());
        data.put("message", "Success");

        return CommonResult.success(data);
    }

    /**
     * 测试2: 带路径参数
     */
    @GetMapping("/param/{id}")
    public CommonResult<Map<String, Object>> withParam(@PathVariable("id") Long id) {
        long count = requestCounter.incrementAndGet();
        log.info("Param request #{}, id={}", count, id);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", count);
        data.put("pathId", id);
        data.put("timestamp", System.currentTimeMillis());

        return CommonResult.success(data);
    }

    /**
     * 测试3: 多级路径参数
     */
    @GetMapping("/multi/{id1}/{id2}")
    public CommonResult<Map<String, Object>> multiParam(
            @PathVariable("id1") Long id1,
            @PathVariable("id2") Long id2) {
        long count = requestCounter.incrementAndGet();
        log.info("Multi param request #{}, id1={}, id2={}", count, id1, id2);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", count);
        data.put("id1", id1);
        data.put("id2", id2);
        data.put("timestamp", System.currentTimeMillis());

        return CommonResult.success(data);
    }

    /**
     * 获取请求统计
     */
    @GetMapping("/stats")
    public CommonResult<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", requestCounter.get());
        stats.put("timestamp", System.currentTimeMillis());

        return CommonResult.success(stats);
    }
}