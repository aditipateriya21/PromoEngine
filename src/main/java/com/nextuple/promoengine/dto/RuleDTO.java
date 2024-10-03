package com.nextuple.promoengine.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleDTO {

    private String id;
    private String name;
    private Map<String, Object> conditions;
    private Map<String, Object> actions;
    private int priority;
    private String status;
    private Date createdAt;

}

