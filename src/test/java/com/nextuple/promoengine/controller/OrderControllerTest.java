package com.nextuple.promoengine.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.model.AppliedRule;
import com.nextuple.promoengine.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.nextuple.promoengine.service.OrderService;
import java.util.Collections;
import java.util.Map;

class OrderControllerTest {

    @Mock
    private  OrderService OrderService;

    @InjectMocks
    private OrderController OrderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAnalyzeOrder_Success() {
        String orderId = "1";

        Rule rule = new Rule();
        rule.setId("rule2");
        rule.setName("Item Quantity Threshold Free Item");
        rule.setConditions(Collections.singletonMap("itemQuantityThreshold",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setActions(Collections.singletonMap("freeItem",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setPriority(2);
        rule.setStatus("active");

        AppliedRule appliedRule = new AppliedRule();
        appliedRule.setRule(rule);
        appliedRule.setActions(Collections.singletonMap("discount", 10.0));

        OrderAnalysisDTO analysisDTO = new OrderAnalysisDTO();
        analysisDTO.setOrderId(orderId);
        analysisDTO.setAppliedRules(Collections.singletonList(appliedRule));

        when(OrderService.analyzeOrder(orderId)).thenReturn(analysisDTO);

        ResponseEntity<OrderAnalysisDTO> response = OrderController.analyzeOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(analysisDTO, response.getBody());
        verify(OrderService).analyzeOrder(orderId);

    }




}

