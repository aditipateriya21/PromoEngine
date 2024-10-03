package com.nextuple.promoengine.controller;


import com.nextuple.promoengine.dto.RuleDTO;
import com.nextuple.promoengine.model.Order;
import com.nextuple.promoengine.model.Rule;
import com.nextuple.promoengine.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/promo")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;



    @PostMapping("/rule")
    public ResponseEntity<RuleDTO> createSingleRule(@RequestBody RuleDTO ruleDTO) {
        RuleDTO createdRuleDTO = ruleService.createSingleRule(ruleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRuleDTO);
    }


    @GetMapping("/rules")
    public ResponseEntity<List<Rule>> getRules() {
        List<Rule> rules = ruleService.getRules();
        return ResponseEntity.status(HttpStatus.OK).body(rules);
    }

    @PostMapping("/apply")
    public ResponseEntity<Order> applyRules(@RequestBody Order order) {
        Order updatedOrder = ruleService.applyRulesToOrder(order);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrder);

    }


    @GetMapping("/rule/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable String id) {

        Rule rule = ruleService.getRuleById(id);
        return ResponseEntity.status(HttpStatus.OK).body(rule);

    }

    @DeleteMapping("/rule/{id}")
    public ResponseEntity<Rule> deleteRuleById(@PathVariable String id) {

        ruleService.deleteRuleById(id);
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @PutMapping("/rule/{id}")
    public ResponseEntity<RuleDTO> updateRule(@PathVariable String id, @RequestBody RuleDTO ruleDTO) {
        RuleDTO updatedRuleDTO = ruleService.updateRule(id, ruleDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedRuleDTO);

    }

}
