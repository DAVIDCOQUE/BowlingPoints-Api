package com.bowlingpoints.service;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.repository.ModalityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModalityServiceTest {

    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private ModalityService modalityService;

    private Modality testModality;
    private ModalityDTO testModalityDTO;

    @BeforeEach
    void setUp() {
        testModality = Modality.builder()
                .modalityId(1)
                .name("Test Modality")
                .description("Test Description")
                .status(true)
                .build();

        testModalityDTO = new ModalityDTO(
                testModality.getModalityId(),
                testModality.getName(),
                testModality.getDescription(),
                testModality.getStatus()
        );
    }

    @Test
    void getAll_WhenModalitiesExist_ShouldReturnSortedList() {
        // Arrange
        Modality modality1 = Modality.builder()
                .modalityId(1)
                .name("A Modality")
                .description("First")
                .status(true)
                .build();

        Modality modality2 = Modality.builder()
                .modalityId(2)
                .name("B Modality")
                .description("Second")
                .status(true)
                .build();

        when(modalityRepository.findAllByOrderByNameAsc())
                .thenReturn(Arrays.asList(modality1, modality2));

        // Act
        List<ModalityDTO> result = modalityService.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("A Modality", result.get(0).getName());
        assertEquals("B Modality", result.get(1).getName());
    }

    @Test
    void getAll_WhenNoModalities_ShouldReturnEmptyList() {
        // Arrange
        when(modalityRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of());

        // Act
        List<ModalityDTO> result = modalityService.getAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getById_WhenModalityExists_ShouldReturnDTO() {
        // Arrange
        when(modalityRepository.findById(1))
                .thenReturn(Optional.of(testModality));

        // Act
        ModalityDTO result = modalityService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testModality.getModalityId(), result.getModalityId());
        assertEquals(testModality.getName(), result.getName());
        assertEquals(testModality.getDescription(), result.getDescription());
        assertEquals(testModality.getStatus(), result.getStatus());
    }

    @Test
    void getById_WhenModalityDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(modalityRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act
        ModalityDTO result = modalityService.getById(99);

        // Assert
        assertNull(result);
    }

    @Test
    void create_WhenValidDTO_ShouldReturnCreatedDTO() {
        // Arrange
        when(modalityRepository.save(any(Modality.class)))
                .thenReturn(testModality);

        ModalityDTO newModalityDTO = new ModalityDTO(
                null,
                "New Modality",
                "New Description",
                true
        );

        // Act
        ModalityDTO result = modalityService.create(newModalityDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testModality.getModalityId(), result.getModalityId());
        assertEquals(testModality.getName(), result.getName());
        assertEquals(testModality.getDescription(), result.getDescription());
        assertEquals(testModality.getStatus(), result.getStatus());

        verify(modalityRepository).save(argThat(modality ->
                modality.getName().equals(newModalityDTO.getName()) &&
                modality.getDescription().equals(newModalityDTO.getDescription()) &&
                modality.getStatus().equals(newModalityDTO.getStatus())
        ));
    }

    @Test
    void update_WhenModalityExists_ShouldReturnTrue() {
        // Arrange
        when(modalityRepository.findById(1))
                .thenReturn(Optional.of(testModality));
        when(modalityRepository.save(any(Modality.class)))
                .thenReturn(testModality);

        ModalityDTO updateDTO = new ModalityDTO(
                1,
                "Updated Name",
                "Updated Description",
                false
        );

        // Act
        boolean result = modalityService.update(1, updateDTO);

        // Assert
        assertTrue(result);
        verify(modalityRepository).save(argThat(modality ->
                modality.getName().equals(updateDTO.getName()) &&
                modality.getDescription().equals(updateDTO.getDescription()) &&
                modality.getStatus().equals(updateDTO.getStatus())
        ));
    }

    @Test
    void update_WhenModalityDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(modalityRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act
        boolean result = modalityService.update(99, testModalityDTO);

        // Assert
        assertFalse(result);
        verify(modalityRepository, never()).save(any());
    }

    @Test
    void delete_WhenModalityExists_ShouldSetDeletedAtAndReturnTrue() {
        // Arrange
        LocalDateTime beforeDelete = LocalDateTime.now();
        when(modalityRepository.findById(1))
                .thenReturn(Optional.of(testModality));

        // Act
        boolean result = modalityService.delete(1);

        // Assert
        assertTrue(result);
        verify(modalityRepository).save(argThat(modality ->
                modality.getDeletedAt() != null &&
                modality.getDeletedAt().isAfter(beforeDelete)
        ));
    }

    @Test
    void delete_WhenModalityDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(modalityRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act
        boolean result = modalityService.delete(99);

        // Assert
        assertFalse(result);
        verify(modalityRepository, never()).save(any());
    }
}