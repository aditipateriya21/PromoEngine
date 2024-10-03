package com.nextuple.promoengine.serviceImpl;


import com.nextuple.promoengine.Exceptions.OrderNotFoundException;
import com.nextuple.promoengine.model.AppliedRule;
import com.nextuple.promoengine.model.Order;
import com.nextuple.promoengine.model.Rule;
import com.nextuple.promoengine.repository.OrderRepository;
import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.repository.RuleRepository;
import com.nextuple.promoengine.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.nextuple.promoengine.service.OrderService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService OrderService;

    @Mock
    private OrderRepository OrderRepository;

    @Mock
    private RuleService ruleService;
    @Mock
    private RuleRepository ruleRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testAnalyzeOrderWhenOrderExists() {
        String orderId = "12345";

        Order order = new Order();
        order.setId(orderId);
        order.setItems(List.of(
                Map.of("id", "item1", "quantity", 2, "price", 10.0),
                Map.of("id", "item2", "quantity", 1, "price", 15.0)
        ));
        order.setOrderTotalBeforeDiscount(35.0);
        order.setDiscountApplied(0.0);
        order.setFinalOrderTotal(35.0);
        order.setActionDetails("No discount applied");

        Rule rule = new Rule();
        rule.setId("rule1");
        rule.setName("Sample Rule");
        rule.setConditions(Collections.singletonMap("itemQuantityThreshold",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setActions(Collections.singletonMap("freeItem",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setPriority(1);
        rule.setStatus("active");

        OrderAnalysisDTO dto = new OrderAnalysisDTO(orderId,
                new ArrayList<>(List.of(new AppliedRule(rule, new HashMap<>()))));

        when(OrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(ruleService.analyzeOrder(order)).thenReturn(dto);

        OrderAnalysisDTO result = OrderService.analyzeOrder(orderId);

        assertNotNull(result);
        assertEquals(dto, result);

        verify(OrderRepository).findById(orderId);
        verify(ruleService).analyzeOrder(order);
    }




    @Test
    void testAnalyzeOrderWhenOrderDoesNotExist() {
        String orderId = "12345";

        when(OrderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException thrown = assertThrows(OrderNotFoundException.class, () -> {
            OrderService.analyzeOrder(orderId);
        });

        assertEquals("Order not found with ID: " + orderId, thrown.getMessage());
        verify(OrderRepository).findById(orderId);
        verify(ruleService, never()).analyzeOrder(any());
    }
}
