package org.example.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendController {
    @GetMapping("/{path:^(?!.*\\.).*$}")
    public String forward(@PathVariable("path") String path) {
        return "forward:/index.html";
    }
}
