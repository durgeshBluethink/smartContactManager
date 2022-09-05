package io.project.smartcontactmanager.controller;

import io.project.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

//    @Autowired
//    private UserRepository userRepository;

    @GetMapping("/home")
    public String hello(){
        return "Testing Hello";
    }
}
