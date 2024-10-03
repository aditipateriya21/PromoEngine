package com.nextuple.promoengine.dto;

import com.nextuple.promoengine.model.AppliedRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalysisDTO {
    private String orderId;
    private List<AppliedRule> appliedRules;
}

