package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.RoleRepository;
import com.bowlingpoints.repository.UserFullRepository;
import com.bowlingpoints.repository.UserRepository;
import com.bowlingpoints.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFullService {

    private final UserFullRepository userFullRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public List<UserFullDTO> getAllUsersWithDetails() {
        return userFullRepository.getUserFullInfoRaw().stream()
                .map(obj -> new UserFullDTO(
                        (Integer) obj[0],
                        (String) obj[1],
                        (String) obj[2],
                        (String) obj[3],
                        (String) obj[4],
                        (String) obj[5],
                        (String) obj[6],
                        (String) obj[7],
                        (String) obj[8],
                        (String) obj[9],
                        null
                ))
                .collect(Collectors.toList());
    }

    public UserFullDTO getUserById(Integer id) {
        return getAllUsersWithDetails().stream()
                .filter(user -> user.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean updateUser(Integer id, UserFullDTO input) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Person person = user.getPerson();

        person.setFirstName(input.getFirstname());
        person.setSecondName(input.getSecondname());
        person.setLastname(input.getLastname());
        person.setSecondLastname(input.getSecondlastname());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());

        personRepository.save(person);

        user.setNickname(input.getNickname());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    public boolean deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setStatus(false);
        user.setUpdatedAt(LocalDateTime.now());

        Person person = user.getPerson();
        person.setStatus(false);
        person.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        personRepository.save(person);

        return true;
    }

    public void createUser(UserFullDTO input) {
        Person person = new Person();
        person.setFirstName(input.getFirstname());
        person.setSecondName(input.getSecondname());
        person.setLastname(input.getLastname());
        person.setSecondLastname(input.getSecondlastname());
        person.setEmail(input.getEmail());
        person.setPhone(input.getPhone());
        person.setGender(input.getGender());
        person.setStatus(true);

        personRepository.save(person);

        User user = new User();
        user.setNickname(input.getNickname());
        user.setPerson(person);
        user.setPassword("encrypted-password"); // default or encryption placeholder
        user.setStatus(true);
        userRepository.save(user);

        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            for (String roleName : input.getRoles()) {
                Optional<Role> role = roleRepository.findByDescription(roleName);
                role.ifPresent(r -> {
                    UserRole userRole = new UserRole();
                    userRole.setUser(user);
                    userRole.setRole(r);
                    userRole.setStatus(true);
                    userRoleRepository.save(userRole);
                });
            }
        }
    }
}
