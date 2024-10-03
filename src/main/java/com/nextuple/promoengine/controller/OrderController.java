package com.nextuple.promoengine.controller;

import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orderNew")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;



    @PostMapping("/analyze/{id}")
    public ResponseEntity<OrderAnalysisDTO> analyzeOrder(@PathVariable("id") String orderId) {
        OrderAnalysisDTO analysis = orderService.analyzeOrder(orderId);
        return ResponseEntity.ok(analysis);
    }


}

