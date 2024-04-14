package com.bowlingpoints.controller;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Login")
public class LoginController {

    @GetMapping
    public String getAll(){
        return "all";
    }
    @PostMapping
    public String create(@RequestBody String test){
        return "test";
    }
}
