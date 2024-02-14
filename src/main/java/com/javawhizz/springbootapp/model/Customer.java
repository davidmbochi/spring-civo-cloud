package com.javawhizz.springbootapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Customer {
    private Long id;

    private String fistName;

    private String lastName;

    private String email;
}
