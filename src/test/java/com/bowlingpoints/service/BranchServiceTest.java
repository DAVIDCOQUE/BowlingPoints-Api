package com.bowlingpoints.service;

import com.bowlingpoints.dto.BranchDTO;
import com.bowlingpoints.entity.Branch;
import com.bowlingpoints.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para BranchService.
 */
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    private Branch branch;
    private BranchDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        branch = Branch.builder()
                .branchId(1)
                .name("Sucursal Norte")
                .description("Rama principal en la zona norte")
                .status(true)
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .build();

        dto = BranchDTO.builder()
                .branchId(1)
                .name("Sucursal Norte")
                .description("Rama principal en la zona norte")
                .status(true)
                .build();
    }

    // ----------------------------------------------------------------------
    // create
    // ----------------------------------------------------------------------
    @Test
    void create_ShouldSaveBranchAndReturnDTO() {
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);

        BranchDTO result = branchService.create(dto);

        assertNotNull(result);
        assertEquals("Sucursal Norte", result.getName());
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void create_ShouldSetStatusTrue_WhenStatusIsNull() {
        BranchDTO input = BranchDTO.builder()
                .name("Nueva Sucursal")
                .description("Descripción")
                .build();

        Branch saved = Branch.builder()
                .branchId(2)
                .name("Nueva Sucursal")
                .description("Descripción")
                .status(true)
                .build();

        when(branchRepository.save(any(Branch.class))).thenReturn(saved);

        BranchDTO result = branchService.create(input);

        assertTrue(result.getStatus());
    }

    // ----------------------------------------------------------------------
    // getAll
    // ----------------------------------------------------------------------
    @Test
    void getAll_ShouldReturnListOfBranches() {
        when(branchRepository.findAll()).thenReturn(List.of(branch));

        List<BranchDTO> result = branchService.getAll();

        assertEquals(1, result.size());
        assertEquals("Sucursal Norte", result.get(0).getName());
        verify(branchRepository).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoBranchesExist() {
        when(branchRepository.findAll()).thenReturn(List.of());

        List<BranchDTO> result = branchService.getAll();

        assertTrue(result.isEmpty());
    }

    // ----------------------------------------------------------------------
    // getActive
    // ----------------------------------------------------------------------
    @Test
    void getActive_ShouldReturnOnlyActiveBranches() {
        when(branchRepository.findAllByStatusTrue()).thenReturn(List.of(branch));

        List<BranchDTO> result = branchService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getStatus());
    }

    // ----------------------------------------------------------------------
    // getById
    // ----------------------------------------------------------------------
    @Test
    void getById_ShouldReturnBranchDTO_WhenExists() {
        when(branchRepository.findById(1)).thenReturn(Optional.of(branch));

        BranchDTO result = branchService.getById(1);

        assertNotNull(result);
        assertEquals("Sucursal Norte", result.getName());
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(branchRepository.findById(999)).thenReturn(Optional.empty());

        BranchDTO result = branchService.getById(999);

        assertNull(result);
    }

    // ----------------------------------------------------------------------
    // update
    // ----------------------------------------------------------------------
    @Test
    void update_ShouldModifyBranch_WhenExists() {
        when(branchRepository.findById(1)).thenReturn(Optional.of(branch));

        BranchDTO updateDTO = BranchDTO.builder()
                .name("Sucursal Sur")
                .description("Actualizada")
                .status(false)
                .build();

        boolean updated = branchService.update(1, updateDTO);

        assertTrue(updated);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void update_ShouldReturnFalse_WhenBranchNotFound() {
        when(branchRepository.findById(99)).thenReturn(Optional.empty());

        boolean updated = branchService.update(99, dto);

        assertFalse(updated);
        verify(branchRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------
    // delete
    // ----------------------------------------------------------------------
    @Test
    void delete_ShouldSoftDeleteBranch_WhenExists() {
        when(branchRepository.findById(1)).thenReturn(Optional.of(branch));

        boolean deleted = branchService.delete(1);

        assertTrue(deleted);
        verify(branchRepository, times(1)).save(any(Branch.class));
        assertFalse(branch.getStatus());
    }

    @Test
    void delete_ShouldReturnFalse_WhenNotFound() {
        when(branchRepository.findById(99)).thenReturn(Optional.empty());

        boolean deleted = branchService.delete(99);

        assertFalse(deleted);
        verify(branchRepository, never()).save(any());
    }
}
