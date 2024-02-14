package com.javawhizz.springbootapp.controller;

import com.javawhizz.springbootapp.model.Customer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class CustomerController {
    private static final List<Customer> CUSTOMERS = List.of(
            new Customer(1L, "john", "doe", "john@javawhizz.com"),
            new Customer(2L, "mary", "public", "mary@javawhizz.com"),
            new Customer(3L, "nelson", "jamal", "nelson@javawhizz.com"),
            new Customer(4L, "Diane", "Phane", "diane@javawhizz.com"),
            new Customer(5L, "steve", "jobs", "steve@javawhizz.com")
    );

    @GetMapping
    public List<Customer> getAllCustomers(){
        return CUSTOMERS;
    }
}
