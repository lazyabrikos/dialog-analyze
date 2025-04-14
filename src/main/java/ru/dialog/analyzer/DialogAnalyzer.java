package ru.dialog.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DialogAnalyzer {
    public static void main(String[] args) {
        SpringApplication.run(DialogAnalyzer.class, args);
    }
}