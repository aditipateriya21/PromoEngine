
package com.nextuple.promoengine.controller;

import com.nextuple.promoengine.service.RuleService;
import com.nextuple.promoengine.model.Rule;
import com.nextuple.promoengine.dto.RuleDTO;
import com.nextuple.promoengine.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RuleControllerTest {

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RuleController ruleController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateSingleRule_Success() {

        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setName("Test Rule");
        ruleDTO.setConditions(Collections.singletonMap("minOrderAmount", 100.0));
        ruleDTO.setActions(Collections.singletonMap("discountPercentage", 10.0));

        RuleDTO createdRuleDTO = new RuleDTO();
        createdRuleDTO.setName("Test Rule");
        createdRuleDTO.setConditions(Collections.singletonMap("minOrderAmount", 100.0));
        createdRuleDTO.setActions(Collections.singletonMap("discountPercentage", 10.0));

        when(ruleService.createSingleRule(ruleDTO)).thenReturn(createdRuleDTO);


        ResponseEntity<RuleDTO> response = ruleController.createSingleRule(ruleDTO);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdRuleDTO, response.getBody());
        verify(ruleService).createSingleRule(ruleDTO);
    }

    @Test
    void testGetRules_Success() {

        Rule rule = new Rule();
        rule.setId("rule1");
        rule.setName("Item Quantity Threshold Free Item");
        rule.setConditions(Collections.singletonMap("itemQuantityThreshold",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setActions(Collections.singletonMap("freeItem",
                Map.of("itemId", "item1", "quantity", 1)));
        rule.setPriority(2);
        rule.setStatus("active");
        List<Rule> rules = Collections.singletonList(rule);

        when(ruleService.getRules()).thenReturn(rules);

        ResponseEntity<List<Rule>> response = ruleController.getRules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rules, response.getBody());
        verify(ruleService).getRules();

    }

    @Test
    void testApplyRules_Success() {
        Order order = new Order();
        order.setId("order123");

        Order updatedOrder = new Order();
        updatedOrder.setId("order123");

        when(ruleService.applyRulesToOrder(order)).thenReturn(updatedOrder);

        ResponseEntity<Order> response = ruleController.applyRules(order);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedOrder, response.getBody());
        verify(ruleService).applyRulesToOrder(order);
    }

    @Test
    void testGetRuleById_Success() {
        String ruleId = "1";
        Rule rule = new Rule();
        rule.setId(ruleId);
        rule.setName("Test Rule");

        when(ruleService.getRuleById(ruleId)).thenReturn(rule);

        ResponseEntity<Rule> response = ruleController.getRuleById(ruleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rule, response.getBody());
    }

    @Test
    void testDeleteRuleById_Success() {
        String ruleId = "1";

        ResponseEntity<Rule> response = ruleController.deleteRuleById(ruleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(ruleService).deleteRuleById(ruleId);
    }


@Test
void testUpdateRule_Success() {
    String ruleId = "1";
    RuleDTO ruleDTO = new RuleDTO();
    ruleDTO.setName("Updated Rule");

    RuleDTO updatedRuleDTO = new RuleDTO();
    updatedRuleDTO.setName("Updated Rule");

    when(ruleService.updateRule(ruleId, ruleDTO)).thenReturn(updatedRuleDTO);

    ResponseEntity<RuleDTO> response = ruleController.updateRule(ruleId, ruleDTO);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(updatedRuleDTO, response.getBody());
}


}
