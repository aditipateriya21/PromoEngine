package com.nextuple.promoengine.serviceImpl;

import static com.nextuple.promoengine.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.nextuple.promoengine.Exceptions.InvalidInputException;
import com.nextuple.promoengine.Exceptions.RuleAlreadyExistsException;
import com.nextuple.promoengine.Exceptions.RuleNotFoundException;
import com.nextuple.promoengine.dto.OrderAnalysisDTO;
import com.nextuple.promoengine.dto.RuleDTO;
import com.nextuple.promoengine.mapper.RuleMapper;
import com.nextuple.promoengine.model.AppliedRule;
import com.nextuple.promoengine.model.Order;
import com.nextuple.promoengine.model.Rule;
import com.nextuple.promoengine.repository.OrderRepository;
import com.nextuple.promoengine.repository.RuleRepository;
import com.nextuple.promoengine.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RuleMapper ruleMapper;


    @InjectMocks
    private RuleService ruleService;

    private Rule sampleRule;
    private Order sampleOrder;
    private RuleDTO sampleRuleDTO;

    private StringBuilder actionDetailsBuilder;

    private static final String FREE_ITEM = "freeItem";
    private static final String ITEM_ID = "itemId";
    private static final String PRICE = "price";
    private static final String RULE_NOT_FOUND_MESSAGE = "Rule not found with ID: ";



    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        actionDetailsBuilder = new StringBuilder();


        sampleRule = new Rule();
        sampleRule.setId("rule123");
        sampleRule.setName("Discount Rule");
        sampleRule.setConditions(new HashMap<>(Map.of("minOrderAmount", 50.0)));
        sampleRule.setActions(new HashMap<>(Map.of("discountPercentage", 10.0)));
        sampleRule.setPriority(1);
        sampleRule.setStatus("active");

        sampleRuleDTO = new RuleDTO();
        sampleRuleDTO.setId("rule123");
        sampleRuleDTO.setName("Discount Rule");
        sampleRuleDTO.setConditions(new HashMap<>(Map.of("minOrderAmount", 50.0)));
        sampleRuleDTO.setActions(new HashMap<>(Map.of("discountPercentage", 10.0)));
        sampleRuleDTO.setPriority(1);
        sampleRuleDTO.setStatus("active");


        sampleOrder = new Order();
        sampleOrder.setId("order123");
        sampleOrder.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of("id", "item1", "quantity", 3, "price", 20.0)),
                new HashMap<>(Map.of("id", "item2", "quantity", 1, "price", 50.0))
        )));
    }

    @Test
    void testGetRules() {
        when(ruleRepository.findAll()).thenReturn(Collections.singletonList(sampleRule));
        List<Rule> rules = ruleService.getRules();
        assertEquals(1, rules.size());
        assertEquals(sampleRule, rules.get(0));
    }


    @Test
    void testApplyRulesToOrderWithNoApplicableRule() {
        when(ruleRepository.findAll()).thenReturn(Collections.emptyList());

        Order resultOrder = ruleService.applyRulesToOrder(sampleOrder);

        assertNotNull(resultOrder);
        assertEquals(110.0, resultOrder.getFinalOrderTotal());
        assertTrue(resultOrder.getActionDetails().contains("Order ID order123 does not qualify for any rules."));
    }

    @Test
    void testUpdateRule() {

        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setName("Updated Rule");
        ruleDTO.setConditions(Collections.singletonMap("minOrderAmount", 200.0));
        ruleDTO.setActions(Collections.singletonMap("discountPercentage", 20.0));
        ruleDTO.setPriority(3);
        ruleDTO.setStatus("active");

        Rule updatedRule = new Rule();
        updatedRule.setId("rule123");
        updatedRule.setName("Updated Rule");
        updatedRule.setConditions(Collections.singletonMap("minOrderAmount", 200.0));
        updatedRule.setActions(Collections.singletonMap("discountPercentage", 20.0));
        updatedRule.setPriority(3);
        updatedRule.setStatus("active");

        when(ruleMapper.ruleDTOToRule(ruleDTO)).thenReturn(updatedRule);
        when(ruleMapper.ruleToRuleDTO(updatedRule)).thenReturn(ruleDTO);

        when(ruleRepository.findById(anyString())).thenReturn(Optional.of(sampleRule));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(invocation -> {
            Rule savedRule = invocation.getArgument(0);
            sampleRule.setName(savedRule.getName());
            sampleRule.setConditions(savedRule.getConditions());
            sampleRule.setActions(savedRule.getActions());
            sampleRule.setPriority(savedRule.getPriority());
            sampleRule.setStatus(savedRule.getStatus());
            return sampleRule;
        });

        RuleDTO resultDTO = ruleService.updateRule("rule123", ruleDTO);

        assertNotNull(resultDTO);
        assertEquals(ruleDTO.getName(), resultDTO.getName());
        assertEquals(ruleDTO.getPriority(), resultDTO.getPriority());
        assertEquals(ruleDTO.getStatus(), resultDTO.getStatus());
        assertEquals(ruleDTO.getConditions(), resultDTO.getConditions());
        assertEquals(ruleDTO.getActions(), resultDTO.getActions());

        verify(ruleRepository).findById(eq("rule123"));
        verify(ruleRepository).save(any(Rule.class));
        verify(ruleMapper).ruleDTOToRule(ruleDTO);
        verify(ruleMapper).ruleToRuleDTO(updatedRule);
    }

    @Test
    void testDeleteRuleById() {
        when(ruleRepository.findById(anyString())).thenReturn(Optional.of(sampleRule));
        doNothing().when(ruleRepository).deleteById(anyString());

        Rule resultRule = ruleService.deleteRuleById("rule123");

        assertNotNull(resultRule);
        assertEquals(sampleRule.getId(), resultRule.getId());
        verify(ruleRepository).deleteById("rule123");
    }

    @Test
    void testAnalyzeOrder() {

        Order sampleOrder = new Order();
        sampleOrder.setId("order123");
        sampleOrder.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of("id", "item1", "quantity", 3, "price", 20.0)),
                new HashMap<>(Map.of("id", "item2", "quantity", 1, "price", 50.0))
        )));


        Rule sampleRule = new Rule();
        sampleRule.setName("Discount Rule");
        sampleRule.setPriority(1);
        sampleRule.setStatus("active");
        sampleRule.setConditions(Map.of("minOrderAmount", Map.of("amount", 50)));
        sampleRule.setActions(Map.of("discou// Total: 50.0ntPercentage", 10));

        AppliedRule appliedRule = new AppliedRule(sampleRule, sampleRule.getActions());


        when(ruleRepository.findAll()).thenReturn(Collections.singletonList(sampleRule));


        OrderAnalysisDTO analysisDTO = ruleService.analyzeOrder(sampleOrder);


        assertNotNull(analysisDTO);
        assertEquals(sampleOrder.getId(), analysisDTO.getOrderId());
        assertFalse(analysisDTO.getAppliedRules().isEmpty());

        assertTrue(analysisDTO.getAppliedRules().contains(appliedRule));
    }


    @Test
    void testCreateSingleRuleAlreadyExists() {

        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setName("Existing Rule");
        ruleDTO.setConditions(Map.of("minOrderAmount", 100.0));

        Rule existingRule = new Rule();
        existingRule.setId("1");
        existingRule.setName("Existing Rule");
        existingRule.setConditions(Map.of("minOrderAmount", 100.0));

        when(ruleRepository.findByName(eq("Existing Rule"))).thenReturn(existingRule);


        assertThrows(RuleAlreadyExistsException.class, () -> ruleService.createSingleRule(ruleDTO));
    }

    @Test
    void testCreateSingleRuleSuccess() {
        when(ruleRepository.findByName(eq(sampleRuleDTO.getName())))
                .thenReturn(null);

        when(ruleMapper.ruleDTOToRule(sampleRuleDTO)).thenReturn(sampleRule);
        when(ruleRepository.save(sampleRule)).thenReturn(sampleRule);
        when(ruleMapper.ruleToRuleDTO(sampleRule)).thenReturn(sampleRuleDTO);

        RuleDTO resultDTO = ruleService.createSingleRule(sampleRuleDTO);

        assertNotNull(resultDTO);
        assertEquals(sampleRuleDTO.getId(), resultDTO.getId());
        assertEquals(sampleRuleDTO.getName(), resultDTO.getName());
        assertEquals(sampleRuleDTO.getConditions(), resultDTO.getConditions());
        assertEquals(sampleRuleDTO.getActions(), resultDTO.getActions());
        assertEquals(sampleRuleDTO.getPriority(), resultDTO.getPriority());
        assertEquals(sampleRuleDTO.getStatus(), resultDTO.getStatus());

        verify(ruleMapper).ruleDTOToRule(sampleRuleDTO);
        verify(ruleRepository).findByName(sampleRuleDTO.getName());
        verify(ruleRepository).save(sampleRule);
        verify(ruleMapper).ruleToRuleDTO(sampleRule);
    }



    @Test
    void testApplyRulesToOrderWithItemQuantityThreshold() {
        Order sampleOrder = new Order();
        sampleOrder.setId("order124");
        sampleOrder.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of("id", "item1", "quantity", 5, "price", 20.0)),
                new HashMap<>(Map.of("id", "item2", "quantity", 1, "price", 50.0))
        )));

        Rule sampleRule = new Rule();
        sampleRule.setName("Item Quantity Threshold Rule");
        sampleRule.setPriority(1);
        sampleRule.setStatus("active");
        sampleRule.setConditions(Map.of("itemQuantityThreshold", Map.of("itemId", "item1", "quantity", 3)));
        sampleRule.setActions(Map.of("discountPercentage", 10));

        when(ruleRepository.findAll()).thenReturn(Collections.singletonList(sampleRule));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order resultOrder = ruleService.applyRulesToOrder(sampleOrder);

        assertNotNull(resultOrder);
        assertEquals(150.0, resultOrder.getOrderTotalBeforeDiscount(), 0.01);
        assertEquals(15.0, resultOrder.getDiscountApplied(), 0.01);
        assertEquals(135.0, resultOrder.getFinalOrderTotal(), 0.01);
        assertTrue(resultOrder.getActionDetails().contains("Discount Applied: 10.0%"));

        verify(orderRepository).save(any(Order.class));
    }





    @Test
    void testApplyRulesToOrderWithValidRule() {
        Order sampleOrder = new Order();
        sampleOrder.setId("order123");
        sampleOrder.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of("id", "item1", "quantity", 3, "price", 20.0)),
                new HashMap<>(Map.of("id", "item2", "quantity", 1, "price", 50.0))
        )));

        double expectedTotal = 110.0;

        Rule sampleRule = new Rule();
        sampleRule.setName("Discount Rule");
        sampleRule.setPriority(1);
        sampleRule.setConditions(Map.of("minOrderAmount", Map.of("amount", 50)));
        sampleRule.setActions(Map.of("discountPercentage", 10));

        when(ruleRepository.findAll()).thenReturn(Collections.singletonList(sampleRule));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order resultOrder = ruleService.applyRulesToOrder(sampleOrder);

        assertNotNull(resultOrder);
        assertEquals(expectedTotal, resultOrder.getOrderTotalBeforeDiscount(), 0.01);
        assertFalse(resultOrder.getActionDetails().contains("Discount Applied: 10.0%"));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testEvaluateItemQuantityThreshold_ItemQuantityMeetsThreshold() {
        Map<String, Object> itemQuantityThreshold = Map.of(
                ITEM_ID, "item1",
                "quantity", 5
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of("id", "item1", "quantity", 3, PRICE, 20.0),
                Map.of("id", "item1", "quantity", 2, PRICE, 20.0)
        )));

        boolean result = ruleService.evaluateItemQuantityThreshold(itemQuantityThreshold, order);

        assertTrue(result);
    }

    @Test
    void testEvaluateBogoCondition_ItemQuantityMeetsThreshold() {
        Map<String, Object> bogoParams = Map.of(
                ITEM_ID, "item1",
                BUY_QUANTITY, 5
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of("id", "item1", "quantity", 3, PRICE, 20.0),
                Map.of("id", "item1", "quantity", 3, PRICE, 20.0)
        )));

        boolean result = ruleService.evaluateBogoCondition(bogoParams, order);

        assertTrue(result);
    }


    @Test
    void testEvaluateBogoCondition_ItemQuantityBelowThreshold() {
        Map<String, Object> bogoParams = Map.of(
                ITEM_ID, "item1",
                BUY_QUANTITY, 6
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of("id", "item1", "quantity", 5, PRICE, 20.0)
        )));

        boolean result = ruleService.evaluateBogoCondition(bogoParams, order);

        assertFalse(result);
    }
    @Test
    void testApplyBogoOffer_NewItemAdded() {
        StringBuilder actionDetailsBuilder = new StringBuilder();

        Map<String, Object> actions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "itemA",
                        BUY_QUANTITY, 1,
                        GET_QUANTITY, 1
                )
        );

        Map<String, Object> conditions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "item1",
                        "buyQuantity", 1,
                        "getQuantity", 1
                )
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 1, PRICE, 20.0))
        )));

        ruleService.applyBogoOffer(actionDetailsBuilder, actions, order, conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 1, PRICE, 20.0))
        ));

        assertEquals(expectedItems, order.getItems());
    }


    @Test
    void testApplyBogoOffer_ExistingItemUpdated() {
        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "item1",
                        BUY_QUANTITY, 2,
                        GET_QUANTITY, 1
                )
        );

        Map<String, Object> conditions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "item1",
                        "buyQuantity", 1,
                        "getQuantity", 1
                )
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 4, PRICE, 20.0))
        )));

        ruleService.applyBogoOffer(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 4, PRICE, 20.0))
        ));

        assertEquals(expectedItems, order.getItems());
    }

    @Test
    void testApplyBogoOffer_NoEligibleItems() {
        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "item1",
                        BUY_QUANTITY, 5,
                        GET_QUANTITY, 2
                )
        );
        Map<String, Object> conditions = Map.of(
                "bogo", Map.of(
                        ITEM_ID, "item1",
                        "buyQuantity", 1,
                        "getQuantity", 1
                )
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 3, PRICE, 20.0))
        )));

        ruleService.applyBogoOffer(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 3, PRICE, 20.0))
        ));

        assertEquals(expectedItems, order.getItems());
        assertTrue(actionDetailsBuilder.toString().isEmpty());
    }


    @Test
    void testApplyBogoOffer_NoBogoOfferProvided() {
        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = Collections.emptyMap();
        Map<String,Object> conditions = Collections.emptyMap();

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of(ITEM_ID, "item1", "quantity", 1, PRICE, 20.0)
        )));

        ruleService.applyBogoOffer(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = List.of(
                Map.of(ITEM_ID, "item1", "quantity", 1, PRICE, 20.0)
        );

        assertEquals(expectedItems, order.getItems());
        assertTrue(actionDetailsBuilder.toString().isEmpty());
    }




    @Test
    void testApplyFreeItem_NoFreeItemAction() {
        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = new HashMap<>();
        Map<String, Object> conditions = new HashMap<>();


        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 2, PRICE, 20.0))
        )));

        ruleService.applyFreeItem(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                new HashMap<>(Map.of(ITEM_ID, "item1", "quantity", 2, PRICE, 20.0))
        ));

        assertEquals(expectedItems, order.getItems());
        assertTrue(actionDetailsBuilder.toString().isEmpty());
    }

    @Test
    void testEvaluateItemQuantityThreshold_ItemQuantityDoesNotMeetThreshold() {
        Map<String, Object> itemQuantityThreshold = Map.of(
                ITEM_ID, "item1",
                "quantity", 7
        );

        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of(ITEM_ID, "item1", "quantity", 3, PRICE, 20.0),
                Map.of(ITEM_ID, "item1", "quantity", 3, PRICE, 20.0)
        )));

        boolean result = ruleService.evaluateItemQuantityThreshold(itemQuantityThreshold, order);

        assertFalse(result);
    }



    @Test
    void testApplyFreeItem_NewItemAdded() {
        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = Map.of(
                FREE_ITEM, Map.of(
                        ITEM_ID, "item1",
                        "quantity", 2
                )
        );

        Map<String, Object> conditions = Map.of(
                "itemQuantityThreshold", Map.of(
                        ITEM_ID, "item1",
                        "quantity", 1

                )
        );


        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of("id", "item1", "quantity", 1, "price", 20.0)
        )));

        ruleService.applyFreeItem(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                Map.of("id", "item1", "quantity", 1, "price", 20.0)
        ));

        assertEquals(expectedItems, order.getItems());
    }



    @Test
    void testApplyFreeItem_NoFreeItem() {

        StringBuilder actionDetailsBuilder = new StringBuilder();
        Map<String, Object> actions = new HashMap<>();
        Map<String, Object> conditions = new HashMap<>();


        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(
                Map.of("id", "item2", "quantity", 1, "price", 20.0)
        )));

        ruleService.applyFreeItem(actionDetailsBuilder, actions, order,conditions);

        List<Map<String, Object>> expectedItems = new ArrayList<>(List.of(
                Map.of("id", "item2", "quantity", 1, "price", 20.0)
        ));

        assertEquals(expectedItems, order.getItems());
        assertTrue(actionDetailsBuilder.toString().isEmpty());
    }




    @Test
    void testApplyRulesToOrderWithBogoCondition() {
        Order sampleOrder = new Order();
        sampleOrder.setId("order125");
        sampleOrder.setItems(new ArrayList<>(List.of(
                new HashMap<>(Map.of("id", "item1", "quantity", 4, "price", 20.0)),
                new HashMap<>(Map.of("id", "item2", "quantity", 1, "price", 50.0))
        )));

        Rule sampleRule = new Rule();
        sampleRule.setName("BOGO Rule");
        sampleRule.setPriority(1);
        sampleRule.setStatus("active");
        sampleRule.setConditions(Map.of("bogo", Map.of("itemId", "item1", "buyQuantity", 2, "getQuantity", 1)));
        sampleRule.setActions(Map.of("discountPercentage", 0));

        when(ruleRepository.findAll()).thenReturn(Collections.singletonList(sampleRule));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order resultOrder = ruleService.applyRulesToOrder(sampleOrder);

        assertNotNull(resultOrder);
        assertEquals(130.0, resultOrder.getOrderTotalBeforeDiscount(), 0.01);
        assertEquals(0.0, resultOrder.getDiscountApplied(), 0.01);
        assertEquals(130.0, resultOrder.getFinalOrderTotal(), 0.01);

        verify(orderRepository).save(any(Order.class));
    }


    @Test
     void testGetRuleById_Success() {
        when(ruleRepository.findById("rule123")).thenReturn(Optional.of(sampleRule));

        Rule result = ruleService.getRuleById("rule123");

        assertNotNull(result);
        assertEquals(sampleRule.getId(), result.getId());
        assertEquals(sampleRule.getName(), result.getName());
        verify(ruleRepository).findById("rule123");
    }

    @Test
    void testGetRuleById_NotFound() {
        when(ruleRepository.findById("invalidId")).thenReturn(Optional.empty());

        RuleNotFoundException exception = assertThrows(RuleNotFoundException.class, () -> {
            ruleService.getRuleById("invalidId");
        });

        assertEquals(RULE_NOT_FOUND_MESSAGE + "invalidId", exception.getMessage());
        verify(ruleRepository).findById("invalidId");
    }

    @Test
    void testUpdateRule_NullRuleDTO() {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            ruleService.updateRule("rule123", null);
        });

        assertEquals("RuleDTO cannot be null", exception.getMessage());
    }

    @Test
     void testProcessRuleActions_WithFreeItem() {
        Rule sampleRule = new Rule();
        sampleRule.setName("Sample Rule");

        Map<String, Object> actions = new HashMap<>();
        actions.put("freeItem", Map.of("itemId", "item1", "quantity", 2));
        sampleRule.setActions(actions);

        AppliedRule appliedRule = ruleService.processRuleActions(sampleRule);

        assertNotNull(appliedRule);
        assertEquals(sampleRule, appliedRule.getRule());
        assertTrue(appliedRule.getActions().containsKey("freeItem"));
        assertEquals("item1", ((Map<String, Object>) appliedRule.getActions().get("freeItem")).get("itemId"));
        assertEquals(2, ((Map<String, Object>) appliedRule.getActions().get("freeItem")).get("quantity"));
    }

    @Test
    void testProcessRuleActions_WithBogo() {
        Rule sampleRule = new Rule();
        sampleRule.setName("Sample Rule");

        Map<String, Object> actions = new HashMap<>();
        actions.put("bogo", Map.of("itemId", "item2", "buyQuantity", 1, "getQuantity", 1));
        sampleRule.setActions(actions);

        AppliedRule appliedRule = ruleService.processRuleActions(sampleRule);

        assertNotNull(appliedRule);
        assertEquals(sampleRule, appliedRule.getRule());
        assertTrue(appliedRule.getActions().containsKey("bogo"));
        assertEquals("item2", ((Map<String, Object>) appliedRule.getActions().get("bogo")).get("itemId"));
        assertEquals(1, ((Map<String, Object>) appliedRule.getActions().get("bogo")).get("buyQuantity"));
        assertEquals(1, ((Map<String, Object>) appliedRule.getActions().get("bogo")).get("getQuantity"));
    }

}






