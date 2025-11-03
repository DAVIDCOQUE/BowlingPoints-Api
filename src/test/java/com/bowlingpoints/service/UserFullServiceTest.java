package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.projection.UserFullProjection;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para UserFullService.
 */
class UserFullServiceTest {

    @Mock private UserFullRepository userFullRepository;
    @Mock private UserRepository userRepository;
    @Mock private PersonRepository personRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PersonCategoryRepository personCategoryRepository;
    @Mock private ClubPersonRepository clubPersonRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserFullService userFullService;

    private UserFullProjection projection;
    private Person person;
    private User user;
    private Role role;
    private Category category;
    private Clubs club;
    private ClubPerson clubPerson;
    private PersonCategory personCategory;
    private UserFullDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        projection = mock(UserFullProjection.class);
        when(projection.getUserId()).thenReturn(1);
        when(projection.getPersonId()).thenReturn(10);
        when(projection.getNickname()).thenReturn("john123");
        when(projection.getDocument()).thenReturn("123456");
        when(projection.getEmail()).thenReturn("john@example.com");
        when(projection.getFullName()).thenReturn("John");
        when(projection.getFullSurname()).thenReturn("Doe");
        when(projection.getPhone()).thenReturn("3001112233");
        when(projection.getGender()).thenReturn("M");
        when(projection.getStatus()).thenReturn(true);
        when(projection.getRoleId()).thenReturn(1);
        when(projection.getRoleName()).thenReturn("JUGADOR");

        person = Person.builder().personId(10).fullName("John").build();
        user = User.builder().userId(1).nickname("john123").status(true).person(person).build();
        role = Role.builder().id(1).name("JUGADOR").build();
        category = Category.builder().categoryId(5).name("Elite").description("Pro").status(true).build();
        club = Clubs.builder().clubId(7).name("Strike Club").build();
        clubPerson = ClubPerson.builder().id(1).person(person).club(club).status(true).build();
        personCategory = PersonCategory.builder().person(person).category(category).status(true).build();

        dto = UserFullDTO.builder()
                .userId(1)
                .nickname("john123")
                .document("123456")
                .email("john@example.com")
                .fullName("John")
                .fullSurname("Doe")
                .password("12345")
                .roles(List.of(new RoleDTO(1, "JUGADOR")))
                .categories(List.of(new CategoryDTO(5, "Elite", "Pro", true)))
                .build();
    }

    // ----------------------------------------------------------------------
    // getAllUsersWithDetails
    // ----------------------------------------------------------------------
    @Test
    void getAllUsersWithDetails_ShouldMapUsersRolesAndCategories() {
        when(userFullRepository.findAllUserFull()).thenReturn(List.of(projection));
        when(personCategoryRepository.findByPerson_PersonId(10)).thenReturn(List.of(personCategory));
        when(clubPersonRepository.findFirstByPerson_PersonIdAndStatusTrue(10)).thenReturn(Optional.of(clubPerson));

        List<UserFullDTO> result = userFullService.getAllUsersWithDetails();

        assertEquals(1, result.size());
        UserFullDTO dto = result.get(0);
        assertEquals("john123", dto.getNickname());
        assertEquals("JUGADOR", dto.getRoles().get(0).getName());
        assertEquals("Elite", dto.getCategories().get(0).getName());
        assertEquals(7, dto.getClubId());
    }

    @Test
    void getAllUsersWithDetails_ShouldHandleEmptyLists() {
        when(userFullRepository.findAllUserFull()).thenReturn(List.of());

        List<UserFullDTO> result = userFullService.getAllUsersWithDetails();

        assertTrue(result.isEmpty());
    }

    // ----------------------------------------------------------------------
    // getAllActivePlayers
    // ----------------------------------------------------------------------
    /*@Test
    void getAllActivePlayers_ShouldReturnOnlyPlayers() {
        UserFullDTO player = UserFullDTO.builder()
                .userId(1)
                .roles(List.of(new RoleDTO(1, "JUGADOR")))
                .build();

        UserFullDTO admin = UserFullDTO.builder()
                .userId(2)
                .roles(List.of(new RoleDTO(2, "ADMIN")))
                .build();

        UserFullService spyService = Mockito.spy(userFullService);
        doReturn(List.of(player, admin)).when(spyService).getAllUsersWithDetails();

        List<UserFullDTO> result = spyService.getAllActivePlayers();

        assertEquals(1, result.size());
        assertEquals("JUGADOR", result.get(0).getRoles().get(0).getName());
    }*/

    // ----------------------------------------------------------------------
    // getUserById / getByUsername
    // ----------------------------------------------------------------------
    @Test
    void getUserById_ShouldReturnMatchingUser() {
        UserFullService spyService = Mockito.spy(userFullService);
        doReturn(List.of(dto)).when(spyService).getAllUsersWithDetails();

        UserFullDTO result = spyService.getUserById(1);

        assertNotNull(result);
        assertEquals("john123", result.getNickname());
    }

    @Test
    void getByUsername_ShouldReturnMatchingUser() {
        UserFullService spyService = Mockito.spy(userFullService);
        doReturn(List.of(dto)).when(spyService).getAllUsersWithDetails();

        UserFullDTO result = spyService.getByUsername("john123");

        assertNotNull(result);
        assertEquals("john123", result.getNickname());
    }

    // ----------------------------------------------------------------------
    // createUser
    // ----------------------------------------------------------------------
    @Test
    void createUser_ShouldSavePersonUserRolesAndCategories() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPass");
        when(userRepository.findAll()).thenReturn(List.of());
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));

        userFullService.createUser(dto);

        verify(personRepository, times(1)).save(any(Person.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
        verify(personCategoryRepository, times(1)).save(any(PersonCategory.class));
    }

   /* @Test
    void createUser_ShouldThrow_WhenPasswordMissing() {
        UserFullDTO invalid = UserFullDTO.builder().password(null).build();
        assertThrows(IllegalArgumentException.class, () -> userFullService.createUser(invalid));
    }

    @Test
    void createUser_ShouldThrow_WhenDuplicateNickname() {
        when(userRepository.findAll()).thenReturn(List.of(User.builder()
                .nickname("john123")
                .status(true)
                .build()));

        UserFullDTO input = UserFullDTO.builder()
                .nickname("john123")
                .document("123456")
                .password("abc")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userFullService.createUser(input));
    }*/

    // ----------------------------------------------------------------------
    // updateUser
    // ----------------------------------------------------------------------
    @Test
    void updateUser_ShouldModifyUserAndRelations() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));

        boolean updated = userFullService.updateUser(1, dto);

        assertTrue(updated);
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(userRoleRepository, atLeastOnce()).deleteByUser_UserId(anyInt());
        verify(personCategoryRepository, atLeastOnce()).deleteAllByPerson_PersonId(anyInt());
    }

    @Test
    void updateUser_ShouldThrow_WhenNicknameExists() {
        User existing = User.builder().userId(2).nickname("john123").status(true).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(existing, user));

        assertThrows(IllegalArgumentException.class, () -> userFullService.updateUser(1, dto));
    }

    @Test
    void updateUser_ShouldReturnFalse_WhenUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        boolean result = userFullService.updateUser(99, dto);
        assertFalse(result);
    }

    // ----------------------------------------------------------------------
    // deleteUser
    // ----------------------------------------------------------------------
    @Test
    void deleteUser_ShouldDeactivateUserPersonRolesAndCategories() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRoleRepository.findAllByUser_UserIdAndStatusTrue(1))
                .thenReturn(List.of(UserRole.builder().user(user).status(true).build()));
        when(personCategoryRepository.findByPerson_PersonId(10))
                .thenReturn(List.of(PersonCategory.builder().person(person).status(true).build()));

        boolean result = userFullService.deleteUser(1);

        assertTrue(result);
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(personRepository, atLeastOnce()).save(any(Person.class));
        verify(userRoleRepository, atLeastOnce()).save(any(UserRole.class));
        verify(personCategoryRepository, atLeastOnce()).save(any(PersonCategory.class));
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        boolean result = userFullService.deleteUser(99);
        assertFalse(result);
    }

    @Test
    void createUser_ShouldThrow_WhenPasswordMissing() {
        UserFullDTO invalid = UserFullDTO.builder()
                .document("123")
                .nickname("test")
                .password("") // contraseña vacía
                .build();

        assertThrows(IllegalArgumentException.class, () -> userFullService.createUser(invalid));
    }

    @Test
    void createUser_ShouldThrow_WhenNicknameMissingAndDocumentMissing() {
        UserFullDTO invalid = UserFullDTO.builder()
                .password("abc")
                .nickname("")
                .document("")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userFullService.createUser(invalid));
    }

    @Test
    void createUser_ShouldThrow_WhenDuplicateNickname() {
        User existing = User.builder()
                .userId(1)
                .nickname("john123")
                .status(true)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(existing));

        UserFullDTO input = UserFullDTO.builder()
                .nickname("john123")
                .document("999")
                .password("12345")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userFullService.createUser(input));
    }
    @Test
    void getAllActiveUsers_ShouldReturnOnlyActiveOnes() {
        UserFullDTO active = UserFullDTO.builder().userId(1).status(true).build();
        UserFullDTO inactive = UserFullDTO.builder().userId(2).status(false).build();

        UserFullService spyService = Mockito.spy(userFullService);
        doReturn(List.of(active, inactive)).when(spyService).getAllUsersWithDetails();

        List<UserFullDTO> result = spyService.getAllActiveUsers();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getStatus());
    }

    @Test
    void getAllActivePlayers_ShouldReturnOnlyPlayers() {
        UserFullDTO player = UserFullDTO.builder()
                .userId(1)
                .roles(List.of(new RoleDTO(1, "JUGADOR")))
                .build();

        UserFullDTO admin = UserFullDTO.builder()
                .userId(2)
                .roles(List.of(new RoleDTO(2, "ADMIN")))
                .build();

        UserFullService spyService = Mockito.spy(userFullService);
        doReturn(List.of(player, admin)).when(spyService).getAllUsersWithDetails();

        List<UserFullDTO> result = spyService.getAllActivePlayers();

        assertEquals(1, result.size());
        assertEquals("JUGADOR", result.get(0).getRoles().get(0).getName());
    }

    @Test
    void deleteUser_ShouldHandleNoRolesOrCategories() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRoleRepository.findAllByUser_UserIdAndStatusTrue(1))
                .thenReturn(Collections.emptyList());
        when(personCategoryRepository.findByPerson_PersonId(10))
                .thenReturn(Collections.emptyList());

        boolean result = userFullService.deleteUser(1);

        assertTrue(result);
        verify(userRepository).save(any(User.class));
        verify(personRepository).save(any(Person.class));
    }

}
