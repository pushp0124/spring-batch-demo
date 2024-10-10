package com.demo.spring.batch;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@NoArgsConstructor
@Setter
public class Order {
    private String orderId;
    private String product;
    private double price;
    private boolean isValid;
}
