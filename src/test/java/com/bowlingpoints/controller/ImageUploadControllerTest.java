package com.bowlingpoints.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ImageUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ImageUploadController.class}) // evita cargar toda la app
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadImage_ShouldReturnImageUrl_WhenFileIsValid() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/api/images/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("http://localhost:9999/uploads/test-image.png"));
    }

    @Test
    void uploadImage_ShouldReturnServerError_WhenIOExceptionOccurs() throws Exception {
        MockMultipartFile mockFile = spy(new MockMultipartFile(
                "file",
                "broken-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "data".getBytes()
        ));

        doThrow(new IOException("Simulated IO Error")).when(mockFile).getInputStream();

        mockMvc.perform(multipart("/api/images/upload").file(mockFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al subir la imagen")));
    }
}
