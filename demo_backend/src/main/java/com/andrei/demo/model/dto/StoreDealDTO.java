package com.andrei.demo.model.dto;

import lombok.Data;

@Data
public class StoreDealDTO {
    private String storeName;
    private String price;
    private String retailPrice;
    private String savingsPercent;
    private String dealUrl;
}
