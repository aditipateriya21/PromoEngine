package com.nextuple.promoengine.model;

import lombok.Data;

import java.util.List;

@Data
public class OrderEvaluationMessage {
    private String orderId;
    private List<String> appliedRules;
    private double orderTotalBeforeDiscount;
    private double discountApplied;
    private double finalOrderTotal;
    private String actionDetails;
    private long timestamp;
}