package com.pigeonkart.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pigeonkart.api.model.Product;
import com.pigeonkart.api.model.ProductCategory;
import com.pigeonkart.api.repository.ProductRepository;

@SpringBootApplication
public class PigeonkartApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigeonkartApiApplication.class, args);
    }
}
