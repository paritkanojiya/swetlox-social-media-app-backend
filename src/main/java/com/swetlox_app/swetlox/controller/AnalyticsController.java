package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.analytics.AnalyticsResponseDto;
import com.swetlox_app.swetlox.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsInfo(@RequestHeader("Authorization") String token){
        AnalyticsResponseDto userAnalyticsDetails = analyticsService.getUserAnalyticsDetails(token);
        return ResponseEntity.ok(userAnalyticsDetails);
    }
}
