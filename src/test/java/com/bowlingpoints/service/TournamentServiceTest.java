package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.exception.BadRequestException;
import com.bowlingpoints.exception.BusinessException;
import com.bowlingpoints.exception.NotFoundException;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @InjectMocks
    private TournamentService tournamentService;

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private AmbitRepository ambitRepository;
    @Mock
    private ModalityRepository modalityRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private TournamentCategoryRepository tournamentCategoryRepository;
    @Mock
    private TournamentModalityRepository tournamentModalityRepository;
    @Mock
    private TournamentBranchRepository tournamentBranchRepository;
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    private Tournament tournament;

    @BeforeEach
    void setUp() {
        Ambit ambit = Ambit.builder()
                .ambitId(1)
                .name("Nacional")
                .build();

        tournament = Tournament.builder()
                .tournamentId(1)
                .name("Torneo de Prueba")
                .organizer("Liga Nacional")
                .ambit(ambit)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(7))
                .status(true)
                .build();
    }

    // ===================== GET ALL =====================

    @Test
    void getAll_ShouldReturnTournamentList() {
        when(tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc())
                .thenReturn(List.of(tournament));
        when(tournamentRegistrationRepository.findByTournament_TournamentId(1))
                .thenReturn(Collections.emptyList());

        List<TournamentDTO> result = tournamentService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tournamentRepository).findAllByDeletedAtIsNullOrderByStartDateDesc();
    }

    @Test
    void getAll_ShouldHandleDatabaseException() {
        when(tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc())
                .thenThrow(new DataAccessResourceFailureException("DB error"));

        assertThrows(BusinessException.class, () -> tournamentService.getAll());
    }

    // ===================== GET BY ID =====================

    @Test
    void getById_ShouldReturnTournament() {
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(tournamentRegistrationRepository.findByTournament_TournamentId(1))
                .thenReturn(Collections.emptyList());

        TournamentDTO result = tournamentService.getById(1);

        assertNotNull(result);
        assertEquals("Torneo de Prueba", result.getName());
    }

    @Test
    void getById_ShouldThrowNotFound() {
        when(tournamentRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> tournamentService.getById(99));
    }

    // ===================== CREATE =====================

    @Test
    void create_ShouldSaveTournamentWithCategoriesModalitiesAndBranches() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Nacional")
                .organizer("Liga de Bowling")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .categoryIds(List.of(1))
                .modalityIds(List.of(2))
                .branches(List.of(BranchDTO.builder().branchId(3).build()))
                .build();

        Tournament saved = Tournament.builder()
                .tournamentId(100)
                .name("Torneo Nacional")
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        Category category = Category.builder().categoryId(1).name("Senior").build();
        Modality modality = Modality.builder().modalityId(2).name("Individual").build();
        Branch branch = Branch.builder().branchId(3).name("Masculina").build();

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(saved);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(2)).thenReturn(Optional.of(modality));
        when(branchRepository.findById(3)).thenReturn(Optional.of(branch));
        when(tournamentRegistrationRepository.findByTournament_TournamentId(anyInt()))
                .thenReturn(Collections.emptyList());

        TournamentDTO result = tournamentService.create(dto);

        assertNotNull(result);
        assertEquals("Torneo Nacional", result.getName());
        verify(tournamentRepository).save(any(Tournament.class));
        verify(categoryRepository).findById(1);
        verify(modalityRepository).findById(2);
        verify(branchRepository).findById(3);
        verify(tournamentCategoryRepository).save(any(TournamentCategory.class));
        verify(tournamentModalityRepository).save(any(TournamentModality.class));
        verify(tournamentBranchRepository).save(any(TournamentBranch.class));
    }

    @Test
    void create_ShouldThrowBadRequest_WhenDtoIsNull() {
        assertThrows(BadRequestException.class, () -> tournamentService.create(null));
    }

    @Test
    void create_ShouldThrowBadRequest_WhenInvalidDates() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Fallido")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        assertThrows(BadRequestException.class, () -> tournamentService.create(dto));
    }

    @Test
    void create_ShouldThrowNotFound_WhenCategoryMissing() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Test")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .categoryIds(List.of(999))
                .build();

        when(tournamentRepository.save(any(Tournament.class)))
                .thenReturn(tournament);
        when(categoryRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tournamentService.create(dto));
    }

    @Test
    void create_ShouldThrowBusinessException_OnDatabaseError() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo BD Error")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        when(tournamentRepository.save(any(Tournament.class)))
                .thenThrow(new DataAccessResourceFailureException("DB error"));

        assertThrows(BusinessException.class, () -> tournamentService.create(dto));
    }

    // ===================== UPDATE =====================

    @Test
    void update_ShouldReturnTrue_WhenTournamentExists() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Actualizado")
                .organizer("Nuevo Org")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .status(true)
                .build();

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(tournamentCategoryRepository.findByTournament_TournamentId(anyInt()))
                .thenReturn(Collections.emptyList());
        when(tournamentModalityRepository.findByTournament_TournamentId(anyInt()))
                .thenReturn(Collections.emptyList());
        when(tournamentBranchRepository.findByTournament_TournamentId(anyInt()))
                .thenReturn(Collections.emptyList());

        boolean result = tournamentService.update(1, dto);
        assertTrue(result);
    }

    @Test
    void update_ShouldReturnFalse_WhenTournamentNotFound() {
        when(tournamentRepository.findById(99)).thenReturn(Optional.empty());
        boolean result = tournamentService.update(99, new TournamentDTO());
        assertFalse(result);
    }

    // ===================== DELETE =====================

    @Test
    void delete_ShouldSoftDeleteTournament() {
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));

        boolean result = tournamentService.delete(1);

        assertTrue(result);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void delete_ShouldReturnFalse_WhenNotFound() {
        when(tournamentRepository.findById(99)).thenReturn(Optional.empty());
        assertFalse(tournamentService.delete(99));
    }

    // ===================== TO DTO =====================

    @Test
    void toDTO_ShouldMapCategoriesModalitiesAndBranches() throws Exception {
        Category category = Category.builder().categoryId(1).name("Senior").status(true).build();
        Modality modality = Modality.builder().modalityId(2).name("Individual").status(true).build();
        Branch branch = Branch.builder().branchId(3).name("Masculina").status(true).build();

        Tournament entity = Tournament.builder()
                .tournamentId(100)
                .name("Torneo Test")
                .categories(List.of(TournamentCategory.builder().category(category).build()))
                .modalities(List.of(TournamentModality.builder().modality(modality).build()))
                .branches(List.of(TournamentBranch.builder().branch(branch).build()))
                .build();

        when(tournamentRegistrationRepository.findByTournament_TournamentId(100))
                .thenReturn(Collections.emptyList());

        var method = TournamentService.class.getDeclaredMethod("toDTO", Tournament.class);
        method.setAccessible(true);
        TournamentDTO dto = (TournamentDTO) method.invoke(tournamentService, entity);

        assertNotNull(dto);
        assertEquals(1, dto.getCategories().size());
        assertEquals(1, dto.getModalities().size());
        assertEquals(1, dto.getBranches().size());
    }

    @Test
    void toDTO_ShouldHandleNullLists() throws Exception {
        Tournament entity = Tournament.builder()
                .tournamentId(200)
                .name("Torneo sin listas")
                .build();

        when(tournamentRegistrationRepository.findByTournament_TournamentId(200))
                .thenReturn(Collections.emptyList());

        var method = TournamentService.class.getDeclaredMethod("toDTO", Tournament.class);
        method.setAccessible(true);
        TournamentDTO dto = (TournamentDTO) method.invoke(tournamentService, entity);

        assertNotNull(dto);
        assertTrue(dto.getCategories().isEmpty());
        assertTrue(dto.getModalities().isEmpty());
        assertTrue(dto.getBranches().isEmpty());
    }

    // ===================== SAVED BRANCHES =====================

    @Test
    void savedBranches_ShouldSaveBranchesSuccessfully() throws Exception {
        BranchDTO branchDTO = BranchDTO.builder().branchId(1).name("Rama A").build();
        Branch branch = Branch.builder().branchId(1).name("Rama A").build();
        List<BranchDTO> branchDTOList = List.of(branchDTO);

        when(branchRepository.findById(1)).thenReturn(Optional.of(branch));
        when(tournamentBranchRepository.save(any(TournamentBranch.class))).thenReturn(null);

        var method = TournamentService.class.getDeclaredMethod("savedBranches", List.class, Tournament.class);
        method.setAccessible(true);
        method.invoke(tournamentService, branchDTOList, tournament);

        verify(branchRepository).findById(1);
        verify(tournamentBranchRepository).save(any(TournamentBranch.class));
    }

    @Test
    void savedBranches_ShouldThrowNotFound_WhenBranchMissing() throws Exception {
        BranchDTO branchDTO = BranchDTO.builder().branchId(999).name("No existe").build();
        List<BranchDTO> branchDTOList = List.of(branchDTO);

        when(branchRepository.findById(999)).thenReturn(Optional.empty());

        var method = TournamentService.class.getDeclaredMethod("savedBranches", List.class, Tournament.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(tournamentService, branchDTOList, tournament)
        );

        // Desempaquetar InvocationTargetException y validar la causa
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof NotFoundException);
        assertEquals("BRANCH_NOT_FOUND", ((NotFoundException) cause).getCode());
    }

    // ===================== GET ALL - CASOS ADICIONALES =====================

    @Test
    void getAll_ShouldReturnEmptyList_WhenRepositoryReturnsNull() {
        when(tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc()).thenReturn(null);

        List<TournamentDTO> result = tournamentService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tournamentRepository).findAllByDeletedAtIsNullOrderByStartDateDesc();
    }

    @Test
    void getAll_ShouldThrowBusinessException_OnUnexpectedError() {
        when(tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc())
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(BusinessException.class, () -> tournamentService.getAll());
    }

    // ===================== GET BY ID - BRANCH COUNTS =====================

    @Test
    void getById_ShouldIncludeBranchPlayerCounts() {
        TournamentBranchPlayerCountDTO countDTO = new TournamentBranchPlayerCountDTO();
        countDTO.setBranchId(1);
        countDTO.setPlayerCount(5L); // ✅ usar Long literal

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(resultRepository.countPlayersByBranch(1))
                .thenReturn(List.of(countDTO));
        when(tournamentRegistrationRepository.findByTournament_TournamentId(1))
                .thenReturn(Collections.emptyList());

        TournamentDTO result = tournamentService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getBranchPlayerCounts().size());
        assertEquals(5L, result.getBranchPlayerCounts().get(0).getPlayerCount());
    }

    // ===================== UPDATE - AMBIT =====================

    @Test
    void update_ShouldUpdateAmbit_WhenAmbitIdPresent() {
        Ambit ambit = Ambit.builder().ambitId(1).name("Internacional").build();
        TournamentDTO dto = TournamentDTO.builder()
                .name("Nuevo Torneo")
                .organizer("Org")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .ambitId(1)
                .build();

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(ambitRepository.findById(1)).thenReturn(Optional.of(ambit));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(tournamentCategoryRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentModalityRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentBranchRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());

        boolean result = tournamentService.update(1, dto);

        assertTrue(result);
        verify(ambitRepository).findById(1);
    }

    @Test
    void update_ShouldWork_WhenListsAreNull() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Sin listas")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(tournamentCategoryRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentModalityRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentBranchRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());

        boolean result = tournamentService.update(1, dto);

        assertTrue(result);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    // ===================== GET BY AMBIT =====================

    @Test
    void getTournamentsByAmbit_ShouldReturnList() {
        when(tournamentRepository.findByAmbit_AmbitIdAndDeletedAtIsNull(1))
                .thenReturn(List.of(tournament));
        when(tournamentRegistrationRepository.findByTournament_TournamentId(1))
                .thenReturn(Collections.emptyList());

        List<TournamentDTO> result = tournamentService.getTournamentsByAmbit(1, "Nacional");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tournamentRepository).findByAmbit_AmbitIdAndDeletedAtIsNull(1);
    }

    // ===================== VALIDATE DATES =====================

    @Test
    void validateDates_ShouldThrow_WhenStartDateIsNull() throws Exception {
        TournamentDTO dto = TournamentDTO.builder()
                .endDate(LocalDate.now().plusDays(2))
                .build();

        var method = TournamentService.class.getDeclaredMethod("validateDates", TournamentDTO.class);
        method.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> method.invoke(tournamentService, dto));
        assertTrue(ex.getCause() instanceof BadRequestException);
        assertEquals("INVALID_START_DATE", ((BadRequestException) ex.getCause()).getCode());
    }

    @Test
    void validateDates_ShouldThrow_WhenEndDateIsNull() throws Exception {
        TournamentDTO dto = TournamentDTO.builder()
                .startDate(LocalDate.now().plusDays(2))
                .build();

        var method = TournamentService.class.getDeclaredMethod("validateDates", TournamentDTO.class);
        method.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> method.invoke(tournamentService, dto));
        assertTrue(ex.getCause() instanceof BadRequestException);
        assertEquals("INVALID_END_DATE", ((BadRequestException) ex.getCause()).getCode());
    }

    @Test
    void validateDates_ShouldThrow_WhenStartDateBeforeToday() throws Exception {
        TournamentDTO dto = TournamentDTO.builder()
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        var method = TournamentService.class.getDeclaredMethod("validateDates", TournamentDTO.class);
        method.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> method.invoke(tournamentService, dto));
        assertTrue(ex.getCause() instanceof BadRequestException);
        assertEquals("INVALID_START_DATE", ((BadRequestException) ex.getCause()).getCode());
    }

    @Test
    void toDTO_ShouldMapTournamentRegistrationsProperly() throws Exception {
        Person person = Person.builder()
                .personId(10)
                .fullName("Juan")
                .fullSurname("Pérez")
                .build();
        Team team = Team.builder()
                .teamId(20)
                .nameTeam("TeamX")
                .build();
        Category category = Category.builder().categoryId(1).name("Senior").build();
        Modality modality = Modality.builder().modalityId(2).name("Individual").build();
        Branch branch = Branch.builder().branchId(3).name("Masculina").build();

        TournamentRegistration reg = TournamentRegistration.builder()
                .registrationId(100)
                .tournament(tournament)
                .person(person)
                .team(team)
                .category(category)
                .modality(modality)
                .branch(branch)
                .status(true)
                .build();

        Tournament entity = Tournament.builder()
                .tournamentId(1)
                .name("Torneo Test")
                .build();

        when(tournamentRegistrationRepository.findByTournament_TournamentId(1))
                .thenReturn(List.of(reg));

        var method = TournamentService.class.getDeclaredMethod("toDTO", Tournament.class);
        method.setAccessible(true);
        TournamentDTO dto = (TournamentDTO) method.invoke(tournamentService, entity);

        assertNotNull(dto.getTournamentRegistrations());
        assertEquals(1, dto.getTournamentRegistrations().size());
        assertEquals("Juan Pérez", dto.getTournamentRegistrations().get(0).getPersonFullName());
    }

    @Test
    void update_ShouldSaveAllCategoriesModalitiesAndBranches() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Completo")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .categoryIds(List.of(1))
                .modalityIds(List.of(2))
                .branchIds(List.of(3))
                .build();

        Category category = Category.builder().categoryId(1).build();
        Modality modality = Modality.builder().modalityId(2).build();
        Branch branch = Branch.builder().branchId(3).build();

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(2)).thenReturn(Optional.of(modality));
        when(branchRepository.findById(3)).thenReturn(Optional.of(branch));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(tournamentCategoryRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentModalityRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());
        when(tournamentBranchRepository.findByTournament_TournamentId(anyInt())).thenReturn(Collections.emptyList());

        boolean result = tournamentService.update(1, dto);

        assertTrue(result);
        verify(categoryRepository).findById(1);
        verify(modalityRepository).findById(2);
        verify(branchRepository).findById(3);
        verify(tournamentCategoryRepository).save(any(TournamentCategory.class));
        verify(tournamentModalityRepository).save(any(TournamentModality.class));
        verify(tournamentBranchRepository).save(any(TournamentBranch.class));
    }

    @Test
    void create_ShouldThrowNotFound_WhenModalityMissing() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Test")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .modalityIds(List.of(99))
                .build();

        when(tournamentRepository.save(any(Tournament.class)))
                .thenReturn(tournament);
        when(modalityRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tournamentService.create(dto));
    }

    @Test
    void create_ShouldThrowNotFound_WhenBranchMissing() {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Torneo Test")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .branches(List.of(BranchDTO.builder().branchId(999).build()))
                .build();

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        when(branchRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tournamentService.create(dto));
    }

    @Test
    void toEntity_ShouldWork_WhenAmbitIsNull() throws Exception {
        TournamentDTO dto = TournamentDTO.builder()
                .name("Sin ámbito")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        var method = TournamentService.class.getDeclaredMethod("toEntity", TournamentDTO.class);
        method.setAccessible(true);
        Tournament entity = (Tournament) method.invoke(tournamentService, dto);

        assertNotNull(entity);
        assertNull(entity.getAmbit());
    }


}
