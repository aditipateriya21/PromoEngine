package com.nextuple.promoengine.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
@Document(collection = "rules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    @Id
    private String id;
    private String name;
    private Map<String, Object> conditions;
    private Map<String, Object> actions;
    private int priority;
    private String status;

    @CreatedDate
    private Date createdAt;
}

