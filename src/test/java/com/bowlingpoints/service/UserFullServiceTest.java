package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserFullServiceTest {

    @Mock
    private UserFullRepository userFullRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ClubPersonRepository clubPersonRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserFullService userFullService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsersWithDetails_ShouldReturnListOfUsers() {
        // Arrange
        Object[] user1 = new Object[]{
            1, // userId
            1, // personId
            "/photo1.jpg", // photoUrl
            "user1", // nickname
            "DOC1", // document
            "user1@test.com", // email
            "John", // fullName
            "Doe", // fullSurname
            Date.valueOf(LocalDate.now()), // birthDate
            "1234567890", // phone
            "M", // gender
            "ADMIN" // roleDescription
        };

        Object[] user2 = new Object[]{
            2, // userId
            2, // personId
            "/photo2.jpg", // photoUrl
            "user2", // nickname
            "DOC2", // document
            "user2@test.com", // email
            "Jane", // fullName
            "Smith", // fullSurname
            Date.valueOf(LocalDate.now()), // birthDate
            "0987654321", // phone
            "F", // gender
            "USER" // roleDescription
        };

        when(userFullRepository.getUserFullInfoRaw()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserFullDTO> result = userFullService.getAllUsersWithDetails();

        // Assert
        assertEquals(2, result.size());
        
        UserFullDTO firstUser = result.get(0);
        assertEquals(1, firstUser.getUserId());
        assertEquals("user1", firstUser.getNickname());
        assertEquals("John", firstUser.getFullName());
        assertEquals("Doe", firstUser.getFullSurname());
        assertEquals("M", firstUser.getGender());
        assertEquals("ADMIN", firstUser.getRoleDescription());

        UserFullDTO secondUser = result.get(1);
        assertEquals(2, secondUser.getUserId());
        assertEquals("user2", secondUser.getNickname());
        assertEquals("Jane", secondUser.getFullName());
        assertEquals("Smith", secondUser.getFullSurname());
        assertEquals("F", secondUser.getGender());
        assertEquals("USER", secondUser.getRoleDescription());
    }

    @Test
    void getByUsername_WhenUserExists_ShouldReturnUserWithClub() {
        // Arrange
        // Create mock data that matches the expected mapping in getAllUsersWithDetails()
        Object[] userData = new Object[]{
            1,                          // userId [0]
            1,                          // personId [1]
            "/photo.jpg",               // photoUrl [2]
            "testuser",                 // nickname [3]
            "DOC1",                     // document [4]
            "test@test.com",           // email [5]
            "Test",                     // fullName [6]
            "User",                     // fullSurname [7]
            Date.valueOf(LocalDate.now()), // birthDate [8]
            "1234567890",              // phone [9]
            "M",                       // gender [10]
            "USER"                     // roleDescription [11]
        };

        when(userFullRepository.getUserFullInfoRaw()).thenReturn(Collections.singletonList(userData));

        Clubs club = Clubs.builder()
                .clubId(1)
                .name("Test Club")
                .build();

        ClubPerson clubPerson = ClubPerson.builder()
                .club(club)
                .person(Person.builder().personId(1).build())
                .status(true)
                .build();

        when(clubPersonRepository.findFirstByPersonAndStatusIsTrue(any())).thenReturn(Optional.of(clubPerson));

        // Act
        UserFullDTO result = userFullService.getByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("testuser", result.getNickname());
        assertEquals(1, result.getClubId());
    }

    @Test
    void getByUsername_WhenUserDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(userFullRepository.getUserFullInfoRaw()).thenReturn(Arrays.asList());

        // Act
        UserFullDTO result = userFullService.getByUsername("nonexistent");

        // Assert
        assertNull(result);
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUserAndPerson() {
        // Arrange
        Integer userId = 1;
        Person person = Person.builder()
                .personId(1)
                .fullName("Old Name")
                .fullSurname("Old Surname")
                .email("old@test.com")
                .build();

        User user = User.builder()
                .userId(userId)
                .nickname("oldnick")
                .person(person)
                .build();

        UserFullDTO updateDto = new UserFullDTO();
        updateDto.setPhotoUrl("/new-photo.jpg");
        updateDto.setDocument("NEW-DOC");
        updateDto.setFullName("New Name");
        updateDto.setFullSurname("New Surname");
        updateDto.setBirthDate(LocalDate.now());
        updateDto.setEmail("new@test.com");
        updateDto.setPhone("9876543210");
        updateDto.setGender("F");
        updateDto.setNickname("newnick");
        updateDto.setPassword("newpassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_newpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(personRepository.save(any(Person.class))).thenReturn(person);

        // Act
        boolean result = userFullService.updateUser(userId, updateDto);

        // Assert
        assertTrue(result);
        
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person updatedPerson = personCaptor.getValue();
        assertEquals("New Name", updatedPerson.getFullName());
        assertEquals("New Surname", updatedPerson.getFullSurname());
        assertEquals("new@test.com", updatedPerson.getEmail());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals("newnick", updatedUser.getNickname());
        assertEquals("encoded_newpassword", updatedUser.getPassword());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Integer userId = 1;
        UserFullDTO updateDto = new UserFullDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        boolean result = userFullService.updateUser(userId, updateDto);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldSoftDeleteUserAndPerson() {
        // Arrange
        Integer userId = 1;
        Person person = Person.builder()
                .personId(1)
                .status(true)
                .build();

        User user = User.builder()
                .userId(userId)
                .person(person)
                .status(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(personRepository.save(any(Person.class))).thenReturn(person);

        // Act
        boolean result = userFullService.deleteUser(userId);

        // Assert
        assertTrue(result);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User deletedUser = userCaptor.getValue();
        assertNotNull(deletedUser.getDeletedAt());
        
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person deletedPerson = personCaptor.getValue();
        assertFalse(deletedPerson.getStatus());
        assertNotNull(deletedPerson.getDeletedAt());
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        boolean result = userFullService.deleteUser(userId);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void createUser_ShouldCreateUserWithPersonAndRoles() {
        // Arrange
        UserFullDTO input = new UserFullDTO();
        input.setPhotoUrl("/photo.jpg");
        input.setDocument("DOC1");
        input.setFullName("New User");
        input.setFullSurname("Test");
        input.setBirthDate(LocalDate.now());
        input.setEmail("new@test.com");
        input.setPhone("1234567890");
        input.setGender("M");
        input.setNickname("newuser");
        input.setPassword("password123");
        input.setRoles(Arrays.asList("ADMIN", "USER"));

        Role adminRole = Role.builder()
                .description("ADMIN")
                .build();

        Role userRole = Role.builder()
                .description("USER")
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(roleRepository.findByDescription("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByDescription("USER")).thenReturn(Optional.of(userRole));
        when(personRepository.save(any(Person.class))).thenAnswer(i -> {
            Person p = i.getArgument(0);
            p.setPersonId(1);
            return p;
        });
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setUserId(1);
            return u;
        });

        // Act
        userFullService.createUser(input);

        // Assert
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals("New User", savedPerson.getFullName());
        assertEquals("Test", savedPerson.getFullSurname());
        assertTrue(savedPerson.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("newuser", savedUser.getNickname());
        assertEquals("encoded_password", savedUser.getPassword());
        assertTrue(savedUser.isStatus());

        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository, times(2)).save(userRoleCaptor.capture());
        List<UserRole> savedUserRoles = userRoleCaptor.getAllValues();
        assertEquals(2, savedUserRoles.size());
        assertTrue(savedUserRoles.stream().allMatch(UserRole::getStatus));
    }

    @Test
    void createUser_WithoutPassword_ShouldThrowIllegalArgumentException() {
        // Arrange
        UserFullDTO input = new UserFullDTO();
        input.setFullName("New User");
        input.setNickname("newuser");
        input.setPassword(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userFullService.createUser(input)
        );

        assertTrue(exception.getMessage().contains("contraseña"));

        verify(personRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void createUser_WithDefaultPhoto_ShouldSetDefaultPhotoUrl() {
        // Arrange
        UserFullDTO input = new UserFullDTO();
        input.setFullName("New User");
        input.setNickname("newuser");
        input.setPassword("password123");
        input.setPhotoUrl("");  // Empty photo URL should trigger default

        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(personRepository.save(any())).thenReturn(new Person());
        when(userRepository.save(any())).thenReturn(new User());

        // Act
        userFullService.createUser(input);

        // Assert
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals("/uploads/users/default.png", savedPerson.getPhotoUrl());
    }
}