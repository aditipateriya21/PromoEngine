package com.nextuple.promoengine.service;

import com.nextuple.promoengine.Exceptions.OrderNotFoundException;
import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.model.Order;
import com.nextuple.promoengine.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


    @Service
    @RequiredArgsConstructor
    public class OrderService {

        private final OrderRepository orderRepository;
        private final RuleService ruleService;



        public OrderAnalysisDTO analyzeOrder(String orderId) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

            return ruleService.analyzeOrder(order);
        }
    }


