package com.afford.avgcalculator.controller;

import com.afford.avgcalculator.dto.ResponseDto;
import com.afford.avgcalculator.service.CalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/numbers")
@RequiredArgsConstructor
@Slf4j
public class CalculatorController {

    private final CalculatorService calculatorService;

    @GetMapping("/{numberId}")
    public ResponseEntity<ResponseDto> getAvgNumber(@PathVariable String numberId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(calculatorService.calculateAvg(numberId));
        } catch (Exception e) {
            log.error("Error calculating data{}", e.getMessage());
            throw new RuntimeException("error calculating");
        }
    }


}
