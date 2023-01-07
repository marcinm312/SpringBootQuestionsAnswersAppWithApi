package pl.marcinm312.springquestionsanswers.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainWebController {

    private static final String MAIN_VIEW = "main";

    @GetMapping
    public String getMainPage() {
        return MAIN_VIEW;
    }
}
