package org.example.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FrontendController {
    @RequestMapping(value = { "/", "/search" }, method = RequestMethod.GET)
    public String index() {
        return "forward:/index.html";
    }
}
