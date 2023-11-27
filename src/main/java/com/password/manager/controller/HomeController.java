package com.password.manager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/welcome")
    public String getResult(){
        return "+-------------------------------------+\n" +
                "|  Welcome to Password Manager!       |\n" +
                "|                                     |\n" +
                "|  Store and manage your passwords    |\n" +
                "|  securely with ease.                |\n" +
                "|                                     |\n" +
                "+-------------------------------------+\n";
    }
}
