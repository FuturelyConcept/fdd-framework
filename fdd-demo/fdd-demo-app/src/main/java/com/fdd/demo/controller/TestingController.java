// fdd-demo/src/main/java/com/fdd/demo/controller/TestingController.java
package com.fdd.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestingController {

    @GetMapping("/fdd-testing")
    public String testingInterface() {
        return "fdd-testing"; // serves fdd-testing.html from templates
    }
}