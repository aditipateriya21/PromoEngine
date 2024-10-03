package com.nextuple.promoengine.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
public class AppliedRule {
    private Rule rule;
    private Map<String, Object> actions;

    public AppliedRule(Rule rule, Map<String, Object> actions) {
        this.rule = rule;
        this.actions = actions;
    }
}

