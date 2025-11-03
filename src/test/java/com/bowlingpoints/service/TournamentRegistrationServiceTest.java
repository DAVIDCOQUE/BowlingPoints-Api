package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentRegistrationDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para TournamentRegistrationService.
 */
class TournamentRegistrationServiceTest {

    @Mock private TournamentRegistrationRepository registrationRepository;
    @Mock private TournamentRepository tournamentRepository;
    @Mock private PersonRepository personRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ModalityRepository modalityRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private TeamRepository teamRepository;

    @InjectMocks
    private TournamentRegistrationService service;

    private Tournament tournament;
    private Person person;
    private Category category;
    private Modality modality;
    private Branch branch;
    private Team team;
    private TournamentRegistration registration;
    private TournamentRegistrationDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tournament = Tournament.builder().tournamentId(1).name("Open Cali").build();
        person = Person.builder().personId(10).fullName("John Doe").build();
        category = Category.builder().categoryId(5).name("Elite").build();
        modality = Modality.builder().modalityId(3).name("Individual").build();
        branch = Branch.builder().branchId(2).name("Rama Norte").build();
        team = Team.builder().teamId(4).nameTeam("Strike Team").build();

        registration = TournamentRegistration.builder()
                .registrationId(100)
                .tournament(tournament)
                .person(person)
                .category(category)
                .modality(modality)
                .branch(branch)
                .team(team)
                .status(true)
                .build();

        dto = TournamentRegistrationDTO.builder()
                .tournamentId(1)
                .personId(10)
                .categoryId(5)
                .modalityId(3)
                .branchId(2)
                .teamId(4)
                .status(true)
                .build();
    }

    // ----------------------------------------------------------------------
    // create
    // ----------------------------------------------------------------------
    @Test
    void create_ShouldSaveAndReturnDTO_WhenValid() {
        when(registrationRepository.existsByTournament_TournamentIdAndPerson_PersonId(1, 10))
                .thenReturn(false);
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(personRepository.findById(10)).thenReturn(Optional.of(person));
        when(categoryRepository.findById(5)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(3)).thenReturn(Optional.of(modality));
        when(branchRepository.findById(2)).thenReturn(Optional.of(branch));
        when(teamRepository.findById(4)).thenReturn(Optional.of(team));
        when(registrationRepository.save(any(TournamentRegistration.class))).thenReturn(registration);

        TournamentRegistrationDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("Open Cali", result.getTournamentName());
        assertEquals("John Doe", result.getPersonFullName());
        assertEquals("Elite", result.getCategoryName());
        verify(registrationRepository).save(any(TournamentRegistration.class));
    }

    @Test
    void create_ShouldThrow_WhenDuplicateExists() {
        when(registrationRepository.existsByTournament_TournamentIdAndPerson_PersonId(1, 10))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(dto));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrow_WhenTournamentNotFound() {
        when(registrationRepository.existsByTournament_TournamentIdAndPerson_PersonId(1, 10))
                .thenReturn(false);
        when(tournamentRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.create(dto));
    }

    // ----------------------------------------------------------------------
    // update
    // ----------------------------------------------------------------------
    @Test
    void update_ShouldModifyAndReturnDTO_WhenExists() {
        when(registrationRepository.findById(100)).thenReturn(Optional.of(registration));
        when(categoryRepository.findById(5)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(3)).thenReturn(Optional.of(modality));
        when(branchRepository.findById(2)).thenReturn(Optional.of(branch));
        when(teamRepository.findById(4)).thenReturn(Optional.of(team));
        when(registrationRepository.save(any())).thenReturn(registration);

        TournamentRegistrationDTO result = service.update(100, dto);

        assertNotNull(result);
        assertEquals("Open Cali", result.getTournamentName());
        verify(registrationRepository).save(any());
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        when(registrationRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(999, dto));
    }

    // ----------------------------------------------------------------------
    // delete
    // ----------------------------------------------------------------------
    @Test
    void delete_ShouldSoftDelete_WhenExists() {
        when(registrationRepository.findById(100)).thenReturn(Optional.of(registration));
        boolean result = service.delete(100);
        assertTrue(result);
        verify(registrationRepository).save(any(TournamentRegistration.class));
    }

    @Test
    void delete_ShouldReturnFalse_WhenNotFound() {
        when(registrationRepository.findById(999)).thenReturn(Optional.empty());
        boolean result = service.delete(999);
        assertFalse(result);
    }

    // ----------------------------------------------------------------------
    // getAll / getByTournament / getByPerson / getById
    // ----------------------------------------------------------------------
    @Test
    void getAll_ShouldReturnList() {
        when(registrationRepository.findByStatusTrue()).thenReturn(List.of(registration));
        List<TournamentRegistrationDTO> list = service.getAll();
        assertEquals(1, list.size());
        assertEquals("Open Cali", list.get(0).getTournamentName());
    }

    @Test
    void getByTournament_ShouldReturnList() {
        when(registrationRepository.findByTournament_TournamentIdAndStatusTrue(1))
                .thenReturn(List.of(registration));
        List<TournamentRegistrationDTO> list = service.getByTournament(1);
        assertEquals(1, list.size());
    }

    @Test
    void getByPerson_ShouldReturnList() {
        when(registrationRepository.findByPerson_PersonIdAndStatusTrue(10))
                .thenReturn(List.of(registration));
        List<TournamentRegistrationDTO> list = service.getByPerson(10);
        assertEquals(1, list.size());
        assertEquals("John Doe", list.get(0).getPersonFullName());
    }

    @Test
    void getById_ShouldReturnMappedDTO_WhenExists() {
        when(registrationRepository.findById(100)).thenReturn(Optional.of(registration));
        TournamentRegistrationDTO result = service.getById(100);
        assertEquals("Open Cali", result.getTournamentName());
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(registrationRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(99));
    }

    // ----------------------------------------------------------------------
// Cobertura de ramas faltantes (null en helpers y relaciones null en toDTO)
// ----------------------------------------------------------------------
    @Test
    void getOptionalMethods_ShouldReturnNull_WhenIdIsNull() throws Exception {
        // accedemos por reflexión para probar métodos privados
        var catMethod = TournamentRegistrationService.class.getDeclaredMethod("getOptionalCategory", Integer.class);
        var modMethod = TournamentRegistrationService.class.getDeclaredMethod("getOptionalModality", Integer.class);
        var branchMethod = TournamentRegistrationService.class.getDeclaredMethod("getOptionalBranch", Integer.class);
        var teamMethod = TournamentRegistrationService.class.getDeclaredMethod("getOptionalTeam", Integer.class);

        catMethod.setAccessible(true);
        modMethod.setAccessible(true);
        branchMethod.setAccessible(true);
        teamMethod.setAccessible(true);

        assertNull(catMethod.invoke(service, (Object) null));
        assertNull(modMethod.invoke(service, (Object) null));
        assertNull(branchMethod.invoke(service, (Object) null));
        assertNull(teamMethod.invoke(service, (Object) null));
    }

    @Test
    void toDTO_ShouldHandleNullRelationsGracefully() throws Exception {
        TournamentRegistration regWithNulls = TournamentRegistration.builder()
                .registrationId(200)
                .tournament(Tournament.builder().tournamentId(1).name("Open Medellín").build())
                .person(Person.builder().personId(20).fullName("Jane Smith").build())
                .category(null)
                .modality(null)
                .branch(null)
                .team(null)
                .status(true)
                .build();

        var method = TournamentRegistrationService.class.getDeclaredMethod("toDTO", TournamentRegistration.class);
        method.setAccessible(true);
        var dtoResult = (TournamentRegistrationDTO) method.invoke(service, regWithNulls);

        assertNotNull(dtoResult);
        assertEquals(1, dtoResult.getTournamentId());
        assertEquals("Jane Smith", dtoResult.getPersonFullName());
        assertNull(dtoResult.getCategoryId());
        assertNull(dtoResult.getModalityId());
        assertNull(dtoResult.getBranchId());
        assertNull(dtoResult.getTeamId());
    }


}
