package com.bowlingpoints.service;

import com.bowlingpoints.dto.PersonaDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonaService {


    @Autowired
    PersonRepository personRepository;


    public List<PersonaDTO> getAllPersona(){

        List<PersonaDTO> personaDTOList = new ArrayList<>();

        List<Person> personaList = personRepository.findAll();

        personaList.forEach(persona -> {
            personaDTOList.add(PersonaDTO.builder()
                            .id(persona.getIdPerson())
                            .email(persona.getEmail())
                            .firstName(persona.getFirstName())
                            .gender(persona.getGender())
                            .lastName(persona.getLastname())
                            .mobile(persona.getPhone())
                            .secondLastName(persona.getSecondLastname())
                            .secondName(persona.getSecondName())

                    .build());
        });


        return personaDTOList;
    }

    public boolean updatePersona(Integer id, PersonaDTO dto) {
        Optional<Person> optionalPersona = personRepository.findById(id);

        if (optionalPersona.isEmpty()) {
            return false;
        }

        Person persona = optionalPersona.get();
        persona.setFirstName(dto.getFirstName());
        persona.setSecondName(dto.getSecondName());
        persona.setLastname(dto.getLastName());
        persona.setSecondLastname(dto.getSecondLastName());
        persona.setGender(dto.getGender());
        persona.setPhone(dto.getMobile());
        persona.setEmail(dto.getEmail());

        personRepository.save(persona);

        return true;
    }

    public boolean deletePersona(Integer id) {
        if (!personRepository.existsById(id)) {
            return false;
        }

        personRepository.deleteById(id);
        return true;
    }


}
