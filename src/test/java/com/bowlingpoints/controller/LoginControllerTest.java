package com.bowlingpoints.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {LoginController.class}) // ✅ Contexto mínimo, evita errores de ApplicationContext
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllOnGet() throws Exception {
        mockMvc.perform(get("/Login"))
                .andExpect(status().isOk())
                .andExpect(content().string("all"));
    }

    @Test
    void shouldReturnTestOnPost() throws Exception {
        mockMvc.perform(post("/Login")
                        .content("data") // contenido cualquiera
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("test"));
    }
}
