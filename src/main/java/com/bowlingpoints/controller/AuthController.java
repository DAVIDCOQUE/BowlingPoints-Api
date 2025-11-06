package com.bowlingpoints.controller;


import com.bowlingpoints.dto.LoginRequest;
import com.bowlingpoints.dto.AuthResponse;
import com.bowlingpoints.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping(value="login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        log.info("Iniciando proceso de inicio de sesion con el usuario->{}",request.getUserName());
        return ResponseEntity.ok(authService.login(request));
    }
}
