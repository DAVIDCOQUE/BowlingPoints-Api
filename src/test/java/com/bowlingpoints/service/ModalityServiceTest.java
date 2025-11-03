package com.bowlingpoints.service;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.repository.ModalityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para ModalityService.
 */
class ModalityServiceTest {

    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private ModalityService modalityService;

    private Modality modality;
    private ModalityDTO modalityDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        modality = Modality.builder()
                .modalityId(1)
                .name("Individual")
                .description("Competencia 1 vs 1")
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        modalityDTO = ModalityDTO.builder()
                .modalityId(1)
                .name("Individual")
                .description("Competencia 1 vs 1")
                .status(true)
                .build();
    }

    // ----------------------------------------------------------------------
    // getAll
    // ----------------------------------------------------------------------
    @Test
    void getAll_ShouldReturnAllModalities_WhenExist() {
        when(modalityRepository.findAllByDeletedAtIsNullOrderByNameAsc())
                .thenReturn(List.of(modality));

        ResponseGenericDTO<List<ModalityDTO>> response = modalityService.getAll();

        assertTrue(response.getSuccess());
        assertEquals("Modalidades cargadas correctamente", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals("Individual", response.getData().get(0).getName());
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoModalitiesExist() {
        when(modalityRepository.findAllByDeletedAtIsNullOrderByNameAsc())
                .thenReturn(List.of());

        ResponseGenericDTO<List<ModalityDTO>> response = modalityService.getAll();

        assertTrue(response.getSuccess());
        assertTrue(response.getData().isEmpty());
    }

    // ----------------------------------------------------------------------
    // getAllActives
    // ----------------------------------------------------------------------
    @Test
    void getAllActives_ShouldReturnOnlyActiveModalities() {
        when(modalityRepository.findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc())
                .thenReturn(List.of(modality));

        ResponseGenericDTO<List<ModalityDTO>> response = modalityService.getAllActives();

        assertTrue(response.getSuccess());
        assertEquals("Modalidades activas cargadas correctamente", response.getMessage());
        assertEquals(1, response.getData().size());
        assertTrue(response.getData().get(0).getStatus());
    }

    // ----------------------------------------------------------------------
    // getById
    // ----------------------------------------------------------------------
    @Test
    void getById_ShouldReturnModality_WhenExists() {
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));

        ResponseGenericDTO<ModalityDTO> response = modalityService.getById(1);

        assertTrue(response.getSuccess());
        assertEquals("Modalidad encontrada", response.getMessage());
        assertEquals("Individual", response.getData().getName());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenDoesNotExist() {
        when(modalityRepository.findById(999)).thenReturn(Optional.empty());

        ResponseGenericDTO<ModalityDTO> response = modalityService.getById(999);

        assertFalse(response.getSuccess());
        assertEquals("Modalidad no encontrada", response.getMessage());
        assertNull(response.getData());
    }

    // ----------------------------------------------------------------------
    // create
    // ----------------------------------------------------------------------
    @Test
    void create_ShouldSaveAndReturnModalityDTO() {
        when(modalityRepository.save(any(Modality.class))).thenReturn(modality);

        ResponseGenericDTO<ModalityDTO> response = modalityService.create(modalityDTO);

        assertTrue(response.getSuccess());
        assertEquals("Modalidad creada correctamente", response.getMessage());
        assertEquals("Individual", response.getData().getName());
        verify(modalityRepository, times(1)).save(any(Modality.class));
    }

    // ----------------------------------------------------------------------
    // update
    // ----------------------------------------------------------------------
    @Test
    void update_ShouldModifyModality_WhenExists() {
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(modalityRepository.save(any(Modality.class))).thenReturn(modality);

        ModalityDTO updatedDTO = ModalityDTO.builder()
                .name("Parejas")
                .description("Competencia en parejas")
                .status(false)
                .build();

        ResponseGenericDTO<Void> response = modalityService.update(1, updatedDTO);

        assertTrue(response.getSuccess());
        assertEquals("Modalidad actualizada correctamente", response.getMessage());
        verify(modalityRepository, times(1)).save(any(Modality.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenDoesNotExist() {
        when(modalityRepository.findById(999)).thenReturn(Optional.empty());

        ResponseGenericDTO<Void> response = modalityService.update(999, modalityDTO);

        assertFalse(response.getSuccess());
        assertEquals("Modalidad no encontrada", response.getMessage());
        verify(modalityRepository, never()).save(any(Modality.class));
    }

    // ----------------------------------------------------------------------
    // delete
    // ----------------------------------------------------------------------
    @Test
    void delete_ShouldSoftDeleteModality_WhenExists() {
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(modalityRepository.save(any(Modality.class))).thenReturn(modality);

        ResponseGenericDTO<Void> response = modalityService.delete(1);

        assertTrue(response.getSuccess());
        assertEquals("Modalidad eliminada correctamente", response.getMessage());
        verify(modalityRepository, times(1)).save(any(Modality.class));
        assertNotNull(modality.getDeletedAt());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenDoesNotExist() {
        when(modalityRepository.findById(999)).thenReturn(Optional.empty());

        ResponseGenericDTO<Void> response = modalityService.delete(999);

        assertFalse(response.getSuccess());
        assertEquals("Modalidad no encontrada", response.getMessage());
        verify(modalityRepository, never()).save(any(Modality.class));
    }
}
