package com.nextuple.promoengine.model;



import lombok.Data;

import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "order")
public class Order {
    @Id
    private String id;

    private List<Map<String, Object>> items;
    private double orderTotalBeforeDiscount;
    private double discountApplied;
    private double finalOrderTotal;
    private String actionDetails;


}
