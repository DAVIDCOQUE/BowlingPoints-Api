package com.bowlingpoints.service;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Ambit;
import com.bowlingpoints.repository.AmbitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmbitServiceTest {

    @Mock
    private AmbitRepository ambitRepository;

    @InjectMocks
    private AmbitService ambitService;

    private Ambit testAmbit;
    private AmbitDTO testAmbitDTO;

    @BeforeEach
    void setUp() {
        testAmbit = Ambit.builder()
                .ambitId(1)
                .name("Test Ambit")
                .description("Test Description")
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testAmbitDTO = AmbitDTO.builder()
                .ambitId(1)
                .name("Test Ambit")
                .description("Test Description")
                .status(true)
                .build();
    }

    @Test
    void getAll_ShouldReturnAllAmbits() {
        // Arrange
        List<Ambit> ambits = Arrays.asList(
            testAmbit,
            Ambit.builder()
                .ambitId(2)
                .name("Test Ambit 2")
                .description("Test Description 2")
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        when(ambitRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(ambits);

        // Act
        ResponseGenericDTO<List<AmbitDTO>> response = ambitService.getAll();

        // Assert
        assertTrue(response.getSuccess(), "Expected response to be successful for getAll");
        assertEquals("Ámbitos cargados correctamente", response.getMessage());
        assertEquals(2, response.getData().size());
        assertEquals("Test Ambit", response.getData().get(0).getName());
        assertEquals("Test Ambit 2", response.getData().get(1).getName());
    }

    @Test
    void getAll_ShouldReturnEmptyList() {
        // Arrange
        when(ambitRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(Collections.emptyList());

        // Act
        ResponseGenericDTO<List<AmbitDTO>> response = ambitService.getAll();

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbitos cargados correctamente", response.getMessage());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getById_WhenAmbitExists_ShouldReturnAmbit() {
        // Arrange
        when(ambitRepository.findById(1)).thenReturn(Optional.of(testAmbit));

        // Act
        ResponseGenericDTO<AmbitDTO> response = ambitService.getById(1);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito encontrado", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testAmbit.getName(), response.getData().getName());
        assertEquals(testAmbit.getDescription(), response.getData().getDescription());
        assertEquals(testAmbit.getStatus(), response.getData().getStatus());
    }

    @Test
    void getById_WhenAmbitDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(ambitRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseGenericDTO<AmbitDTO> response = ambitService.getById(999);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Ámbito no encontrado", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void create_ShouldSaveAndReturnNewAmbit() {
        // Arrange
        AmbitDTO newAmbitDTO = AmbitDTO.builder()
                .name("New Ambit")
                .description("New Description")
                .status(true)
                .build();

        Ambit savedAmbit = Ambit.builder()
                .ambitId(1)
                .name("New Ambit")
                .description("New Description")
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(ambitRepository.save(any(Ambit.class))).thenReturn(savedAmbit);

        // Act
        ResponseGenericDTO<AmbitDTO> response = ambitService.create(newAmbitDTO);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito creado correctamente", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(newAmbitDTO.getName(), response.getData().getName());
        assertEquals(newAmbitDTO.getDescription(), response.getData().getDescription());
        assertEquals(newAmbitDTO.getStatus(), response.getData().getStatus());

        verify(ambitRepository).save(any(Ambit.class));
    }

    @Test
    void create_WithNullStatus_ShouldSetDefaultStatusTrue() {
        // Arrange
        AmbitDTO newAmbitDTO = AmbitDTO.builder()
                .name("New Ambit")
                .description("New Description")
                .status(null)
                .build();

        Ambit savedAmbit = Ambit.builder()
                .ambitId(1)
                .name("New Ambit")
                .description("New Description")
                .status(true) // Default status should be true
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(ambitRepository.save(any(Ambit.class))).thenReturn(savedAmbit);

        // Act
        ResponseGenericDTO<AmbitDTO> response = ambitService.create(newAmbitDTO);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito creado correctamente", response.getMessage());
        assertTrue(response.getData().getStatus());
    }

    @Test
    void update_WhenAmbitExists_ShouldUpdateFields() {
        // Arrange
        Integer id = 1;
        AmbitDTO updateDTO = AmbitDTO.builder()
                .name("Updated Name")
                .description("Updated Description")
                .status(false)
                .build();

        when(ambitRepository.findById(id)).thenReturn(Optional.of(testAmbit));
        when(ambitRepository.save(any(Ambit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseGenericDTO<Void> response = ambitService.update(id, updateDTO);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito actualizado correctamente", response.getMessage());
        verify(ambitRepository).save(argThat(ambit -> 
            ambit.getName().equals("Updated Name") &&
            ambit.getDescription().equals("Updated Description") &&
            !ambit.getStatus()
        ));
    }

    @Test
    void update_WhenAmbitDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        Integer id = 999;
        AmbitDTO updateDTO = AmbitDTO.builder()
                .name("Updated Name")
                .description("Updated Description")
                .status(false)
                .build();

        when(ambitRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseGenericDTO<Void> response = ambitService.update(id, updateDTO);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Ámbito no encontrado", response.getMessage());
        verify(ambitRepository, never()).save(any());
    }

    @Test
    void update_WithNoChanges_ShouldNotUpdateTimestamp() {
        // Arrange
        Integer id = 1;
        AmbitDTO updateDTO = AmbitDTO.builder()
                .name(testAmbit.getName())
                .description(testAmbit.getDescription())
                .status(testAmbit.getStatus())
                .build();

        LocalDateTime originalUpdateTime = testAmbit.getUpdatedAt();
        when(ambitRepository.findById(id)).thenReturn(Optional.of(testAmbit));
        when(ambitRepository.save(any(Ambit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseGenericDTO<Void> response = ambitService.update(id, updateDTO);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito actualizado correctamente", response.getMessage());
        assertEquals(originalUpdateTime, testAmbit.getUpdatedAt());
    }

    @Test
    void delete_WhenAmbitExists_ShouldSoftDelete() {
        // Arrange
        Integer id = 1;
        when(ambitRepository.findById(id)).thenReturn(Optional.of(testAmbit));
        when(ambitRepository.save(any(Ambit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseGenericDTO<Void> response = ambitService.delete(id);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Ámbito eliminado correctamente", response.getMessage());
        verify(ambitRepository).save(argThat(ambit -> 
            !ambit.getStatus() &&
            ambit.getDeletedAt() != null &&
            ambit.getUpdatedAt() != null
        ));
    }

    @Test
    void delete_WhenAmbitDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        Integer id = 999;
        when(ambitRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseGenericDTO<Void> response = ambitService.delete(id);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Ámbito no encontrado", response.getMessage());
        verify(ambitRepository, never()).save(any());
    }
}