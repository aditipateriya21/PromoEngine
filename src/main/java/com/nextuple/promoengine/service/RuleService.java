package com.nextuple.promoengine.service;

import com.nextuple.promoengine.Exceptions.InvalidInputException;
import com.nextuple.promoengine.Exceptions.RuleAlreadyExistsException;
import com.nextuple.promoengine.Exceptions.RuleNotFoundException;
import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.dto.RuleDTO;
import com.nextuple.promoengine.mapper.RuleMapper;
import com.nextuple.promoengine.model.AppliedRule;
import com.nextuple.promoengine.model.Order;
import com.nextuple.promoengine.model.OrderEvaluationMessage;
import com.nextuple.promoengine.model.Rule;
import com.nextuple.promoengine.repository.OrderRepository;
import com.nextuple.promoengine.repository.RuleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.nextuple.promoengine.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService  {
    private final RuleRepository ruleRepository;
    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducer;
    private final RuleMapper ruleMapper ;


    private final ExecutorService executorService = Executors.newFixedThreadPool(10);



    public List<Rule> getRules() {
        log.info("Inside getRules Method");
          return  ruleRepository.findAll();

    }


//    apply rules method
    public Order applyRulesToOrder(Order order) {
        log.info(" Inside applyRulesToOrder() Applying rules to order ID: {} ", order.getId());
        validateOrder(order);

        List<Rule> rules = getRules();
        List<AppliedRule> appliedRules = evaluateRules(order,rules);

        AppliedRule highestPriorityRule = getHighestPriorityRule(appliedRules);
        double orderTotal = calculateOrderTotal(order);

        return processOrder(order, highestPriorityRule, orderTotal, appliedRules);

    }



    public void validateOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new InvalidInputException("Order cannot be null and must have an ID");
        }
        log.info("Order validated successfully: {}", order.getId());
    }

    private AppliedRule getHighestPriorityRule(List<AppliedRule> appliedRules) {
        log.info("Inside getHighestPriorityRule() Determining highest priority rule.");
        appliedRules.sort((rule1, rule2) -> Integer.compare(rule1.getRule().getPriority(), rule2.getRule().getPriority()));
        return appliedRules.isEmpty() ? null : appliedRules.get(0);
    }



public Order processOrder(Order order, AppliedRule highestPriorityRule, double orderTotal, List<AppliedRule> appliedRules) {
    log.info("Inside  processOrder() Processing order with ID: {} in processOrder method", order.getId());

    double discountPercentage = 0;
    double finalOrderTotal = orderTotal;
    StringBuilder actionDetailsBuilder = new StringBuilder();

    if (highestPriorityRule != null) {
        Map<String, Object> actions = highestPriorityRule.getActions();
        Map<String,Object> conditions = highestPriorityRule.getRule().getConditions();
        log.info("{}",conditions);
        discountPercentage = applyDiscount(actionDetailsBuilder, actions);
        finalOrderTotal -= (discountPercentage / 100) * orderTotal;

        applyFreeItem(actionDetailsBuilder, actions, order,conditions);
        applyBogoOffer(actionDetailsBuilder, actions, order,conditions);
    } else {
        handleNoApplicableRules(order, actionDetailsBuilder);
    }

    finalizeOrder(order, orderTotal, discountPercentage, finalOrderTotal, actionDetailsBuilder, appliedRules);
    return order;
}


    private double applyDiscount(StringBuilder actionDetailsBuilder, Map<String, Object> actions) {
        log.info("Inside applyDiscount()  Applying discount.");
        double discountPercentage = 0;
        if (actions.containsKey("discountPercentage")) {
            discountPercentage = ((Number) actions.get("discountPercentage")).doubleValue();
            actionDetailsBuilder.append("Discount Applied: ").append(discountPercentage).append("%");
        }
        return discountPercentage;
    }


public void applyFreeItem(StringBuilder actionDetailsBuilder, Map<String, Object> actions, Order order, Map<String,Object> conditions) {
    log.info("Inside applyFreeItem Applying free item action.");

    if (actions.containsKey(FREE_ITEM)) {
        Map<String, Object> freeItemAction = (Map<String, Object>) actions.get(FREE_ITEM);
        String freeItemId = (String) freeItemAction.get(ITEM_ID);
        int freeItemQuantity = ((Number) freeItemAction.get("quantity")).intValue();

        String conditionItemId  = (String)((Map<String, Object>) conditions.get("itemQuantityThreshold")).get("itemId");
        int requiredQuantity = ((Number) ((Map<String, Object>) conditions.get("itemQuantityThreshold")).get("quantity")).intValue();

        int currentQuantity = order.getItems().stream()
                .filter(item -> conditionItemId.equals(item.get("id")))
                .mapToInt(item -> ((Number) item.get("quantity")).intValue())
                .sum();

        if (currentQuantity >= requiredQuantity) {
            boolean itemAlreadyInOrder = order.getItems().stream()
                    .anyMatch(item -> freeItemId.equals(item.get("id")));
            if (!itemAlreadyInOrder) {
                order.getItems().add(Map.of(
                        "id", freeItemId,
                        "quantity", freeItemQuantity,
                        PRICE, 0.0
                ));
                actionDetailsBuilder.append("\nFree Item Added: ").append(freeItemId).append(" (Quantity: ").append(freeItemQuantity).append(")");
            } else {
                order.getItems().stream()
                        .filter(item -> freeItemId.equals(item.get("id")))
                        .forEach(item -> {
                            int existingQuantity = ((Number) item.get("quantity")).intValue();

                        });
                actionDetailsBuilder.append("\nFree Item Quantity Increased: ").append(freeItemId).append(" (Additional Quantity: ").append(freeItemQuantity).append(")");
            }
        } else {
            actionDetailsBuilder.append("\nCondition not met for free item: required ").append(requiredQuantity).append(" of ").append(conditionItemId).append(", but found ").append(currentQuantity).append(".");
        }
    }
}



    public void applyBogoOffer(StringBuilder actionDetailsBuilder, Map<String, Object> actions, Order order, Map<String, Object> conditions) {
        log.info("Inside applyBogoOffer() Applying BOGO offer.");

        if (actions.containsKey("bogo")) {
            Map<String, Object> bogoAction = (Map<String, Object>) actions.get("bogo");

            String conditionItemId = (String) ((Map<String, Object>) conditions.get("bogo")).get("itemId");
            String bogoItemId = (String) bogoAction.get(ITEM_ID);
            int bogoBuyQuantity = ((Number) ((Map<String, Object>) conditions.get("bogo")).get(BUY_QUANTITY)).intValue();
            int bogoGetQuantity = ((Number) ((Map<String, Object>) conditions.get("bogo")).get(GET_QUANTITY)).intValue();

            List<Map<String, Object>> itemsToAdd = new ArrayList<>();

            order.getItems().stream()
                    .filter(item -> conditionItemId.equals(item.get("id")))
                    .forEach(item -> {
                        int currentQuantity = ((Number) item.get("quantity")).intValue();
                        if (currentQuantity >= bogoBuyQuantity) {
                            int freeItems = (currentQuantity / bogoBuyQuantity) * bogoGetQuantity;
                            boolean itemAlreadyInOrder = order.getItems().stream()
                                    .anyMatch(orderItem -> bogoItemId.equals(orderItem.get("id")));

                            if (!itemAlreadyInOrder) {
                                itemsToAdd.add(Map.of(
                                        "id", bogoItemId,
                                        "quantity", freeItems,
                                        PRICE, 0.0
                                ));
                                actionDetailsBuilder.append("\nBOGO Offer Applied: Buy ").append(bogoBuyQuantity).append(", Get ").append(freeItems).append(" free of ").append(bogoItemId);
                            } else {
                                order.getItems().stream()
                                        .filter(orderItem -> bogoItemId.equals(orderItem.get("id")))
                                        .forEach(orderItem -> {
                                            int existingQuantity = ((Number) orderItem.get("quantity")).intValue();
                                            // Update quantity logic here (if needed)
                                        });
                                actionDetailsBuilder.append("\nBOGO Offer Quantity Increased: ").append(bogoItemId).append(" (Additional Quantity: ").append(freeItems).append(")");
                            }
                        }
                    });

            order.getItems().addAll(itemsToAdd);
        }
    }


    private void handleNoApplicableRules(Order order, StringBuilder actionDetailsBuilder) {
        log.info("No applicable rules for order ID: {}", order.getId());
        actionDetailsBuilder.append("Order ID ").append(order.getId()).append(" does not qualify for any rules.");
    }

private void finalizeOrder(Order order, double orderTotal, double discountPercentage, double finalOrderTotal, StringBuilder actionDetailsBuilder, List<AppliedRule> appliedRules) {
    order.setOrderTotalBeforeDiscount(orderTotal);
    order.setDiscountApplied(Double.parseDouble(String.format("%.2f", (discountPercentage / 100) * orderTotal)));
    order.setFinalOrderTotal(finalOrderTotal);
    order.setActionDetails(actionDetailsBuilder.toString());

    log.info("Order ID {} finalized: Total Before Discount: {}, Discount Applied: {}, Final Total: {}",
            order.getId(), orderTotal, order.getDiscountApplied(), finalOrderTotal);

    // Create Kafka message
    OrderEvaluationMessage message = new OrderEvaluationMessage();
    message.setOrderId(order.getId());
    message.setAppliedRules(appliedRules.stream()
            .map(appliedRule -> appliedRule.getRule().getName())
            .collect(Collectors.toList()));

    message.setOrderTotalBeforeDiscount(orderTotal);
    message.setDiscountApplied(order.getDiscountApplied());
    message.setFinalOrderTotal(finalOrderTotal);
    message.setActionDetails(actionDetailsBuilder.toString());
    message.setTimestamp(System.currentTimeMillis());

    // Send asynchronously
    sendMessageToKafka(message);

    orderRepository.save(order);
}




public List<AppliedRule> evaluateRules(Order order, List<Rule> rules) {
    log.info("Inside evaluateRules()  Evaluating rules for order ID: {}", order.getId());
    return rules.stream()
            .filter(rule -> "active".equals(rule.getStatus()))
            .filter(rule -> evaluateRuleConditions(rule, order))
            .map(this::processRuleActions)
            .collect(Collectors.toList());
}


    private boolean evaluateRuleConditions(Rule rule, Order order) {
        log.info("Inside evaluateRuleConditions() Evaluating conditions for rule: {}", rule.getName());

        return rule.getConditions().entrySet().stream()
                .allMatch(condition -> evaluateCondition(condition, order));
    }

    private boolean evaluateCondition(Map.Entry<String, Object> condition, Order order) {
        String key = condition.getKey();
        Object value = condition.getValue();
        log.info("Evaluating condition: {} with value: {}", key, value);



        switch (key) {
            case "minOrderAmount":
                return evaluateMinOrderAmount((Map<String, Object>) value, order);

            case "itemQuantityThreshold":
                return evaluateItemQuantityThreshold((Map<String, Object>) value, order);
                case "bogo":
                return evaluateBogoCondition((Map<String, Object>) value, order);


            default:
                return false;
        }
    }

private boolean evaluateMinOrderAmount(Map<String, Object> value, Order order) {
    log.info("Evaluating minimum order amount.");

    double minOrderAmount = ((Number) value.get("amount")).doubleValue();
    double totalAmount = calculateOrderTotal(order);
    return totalAmount >= minOrderAmount;
}


    public boolean evaluateItemQuantityThreshold(Map<String, Object> itemQuantityThreshold, Order order) {
        log.info("Evaluating item quantity threshold.");

        String itemId = (String) itemQuantityThreshold.get(ITEM_ID);
        int requiredQuantity = ((Number) itemQuantityThreshold.get("quantity")).intValue();

          int totalQuantity = order.getItems().stream()
                .filter(item -> itemId.equals(item.get("id")))
                .mapToInt(item -> ((Number) item.get("quantity")).intValue())
                        .sum();
          log.info("Item ID: {}, Required Quantity: {}, Total Quantity: {}", itemId, requiredQuantity, totalQuantity);

        return totalQuantity >= requiredQuantity;
    }

    public boolean evaluateBogoCondition(Map<String, Object> bogoParams, Order order) {
        log.info("Evaluating BOGO condition.");
        String bogoItemId = (String) bogoParams.get(ITEM_ID);
        int bogoBuyQuantity = ((Number) bogoParams.get(BUY_QUANTITY)).intValue();

        return order.getItems().stream()
                .filter(item -> bogoItemId.equals(item.get("id")))
                .mapToInt(item -> ((Number) item.get("quantity")).intValue())
                .sum() >= bogoBuyQuantity;
    }

    public AppliedRule processRuleActions(Rule rule) {
        log.info("Processing actions for rule: {}", rule.getName());

        Map<String, Object> actions = new HashMap<>(rule.getActions());
        log.info("actions: {}",actions);

        if (actions.containsKey(FREE_ITEM)) {
            actions.put(FREE_ITEM, processFreeItemAction((Map<String, Object>) actions.get(FREE_ITEM)));
            actions.put(FREE_ITEM, processFreeItemAction((Map<String, Object>) actions.get(FREE_ITEM)));

            log.info("actions after processing: {}",actions);
        }

        if (actions.containsKey("bogo")) {
            actions.put("bogo", processBogoAction((Map<String, Object>) actions.get("bogo")));
        }

        return new AppliedRule(rule, actions);
    }

    private Map<String, Object> processFreeItemAction(Map<String, Object> freeItemAction) {
        String freeItemId = (String) freeItemAction.get(ITEM_ID);
        int freeItemQuantity = ((Number) freeItemAction.get("quantity")).intValue();
        return Map.of("itemId", freeItemId, "quantity", freeItemQuantity);
    }

    private Map<String, Object> processBogoAction(Map<String, Object> bogoAction) {
        String bogoItemId = (String) bogoAction.get(ITEM_ID);
        int bogoBuyQuantity = ((Number) bogoAction.get(BUY_QUANTITY)).intValue();
        int bogoGetQuantity = ((Number) bogoAction.get(GET_QUANTITY)).intValue();
        return Map.of("itemId", bogoItemId, "buyQuantity", bogoBuyQuantity, "getQuantity", bogoGetQuantity);
    }

    public double calculateOrderTotal(Order order) {
        return  order.getItems().stream()
                .mapToDouble(item -> {
                    double price = ((Number) item.get("price")).doubleValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    return price * quantity;
                })
                .sum();

    }



// end of apply rule logic



    public RuleDTO createSingleRule(RuleDTO ruleDTO) {
        log.info("Creating rule: {}", ruleDTO.getName());

//        if (ruleDTO == null) {
//            throw new InvalidInputException("RuleDTO cannot be null");
//        }
        Rule existingRule = ruleRepository.findByName(ruleDTO.getName());
        if (existingRule != null) {
            throw new RuleAlreadyExistsException("Rule already exists with the same name");
        }
        Rule rule = ruleMapper.ruleDTOToRule(ruleDTO);
        Rule savedRule = ruleRepository.save(rule);
        return ruleMapper.ruleToRuleDTO(savedRule);
    }

    public OrderAnalysisDTO analyzeOrder(Order order) {
        log.info("Inside analyzeOrder() Analyzing order ID: {}", order.getId());
        if (/*order == null || */order.getId() == null) {
            throw new InvalidInputException("Order cannot be null and must have an ID");
        }
        List<Rule> rules = ruleRepository.findAll();
        List<AppliedRule> appliedRules = evaluateRules(order, rules);
        return new OrderAnalysisDTO(order.getId(), appliedRules);
    }



    public Rule getRuleById(String id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(RULE_NOT_FOUND_MESSAGE+ id));
    }

    public Rule deleteRuleById(String id) {
        log.info("Inside deleteRuleById() Deleting rule by ID: {}", id);
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(RULE_NOT_FOUND_MESSAGE + id));
        ruleRepository.deleteById(id);
        return rule;
    }



        public RuleDTO updateRule(String id, RuleDTO ruleDTO) {
            log.info("Updating rule ID: {}", id);
            if (ruleDTO == null) {
                throw new InvalidInputException("RuleDTO cannot be null");
            }

            Rule rule = ruleMapper.ruleDTOToRule(ruleDTO);

            Rule existingRule = ruleRepository.findById(id)
                    .orElseThrow(() -> new RuleNotFoundException(RULE_NOT_FOUND_MESSAGE+ id));

            existingRule.setName(rule.getName());
            existingRule.setConditions(rule.getConditions());
            existingRule.setActions(rule.getActions());
            existingRule.setPriority(rule.getPriority());
            existingRule.setStatus(rule.getStatus());

            Rule updatedRule = ruleRepository.save(existingRule);

            return ruleMapper.ruleToRuleDTO(updatedRule);
        }


    private void sendMessageToKafka(OrderEvaluationMessage message) {
        executorService.submit(() -> {
            try {
                String jsonMessage = convertToJson(message);
                kafkaProducer.sendMessage("order-qualification-topic", jsonMessage);
            } catch (Exception e) {
                log.error("Error sending message to Kafka: {}" , e.getMessage());
            }
        });
    }

    private String convertToJson(OrderEvaluationMessage message) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(message);
    }




}


