package com.smartcompany.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcompany.service.UserService;

@RestController
public class ActivationTokenController {

    @Autowired
    private UserService userService;

    @PostMapping("/activate/{username}")
    public ResponseEntity<String> activateAccount(@RequestParam  String token, @PathVariable String username) {
        if (userService.validateActivationToken(username, token)) {
            userService.activateUser(username);
            return ResponseEntity.ok("Account activated successfully!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired activation token.");
    }
    
}
