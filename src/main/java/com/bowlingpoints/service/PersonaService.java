package com.bowlingpoints.service;

import com.bowlingpoints.dto.PersonaDTO;
import com.bowlingpoints.entity.Persona;
import com.bowlingpoints.entity.Ronda;
import com.bowlingpoints.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonaService {


    @Autowired
    PersonaRepository personaRepository;


    public List<PersonaDTO> getAllPersona(){

        List<PersonaDTO> personaDTOList = new ArrayList<>();

        List<Persona> personaList = personaRepository.findAll();

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
        Optional<Persona> optionalPersona = personaRepository.findById(id);

        if (optionalPersona.isEmpty()) {
            return false;
        }

        Persona persona = optionalPersona.get();
        persona.setFirstName(dto.getFirstName());
        persona.setSecondName(dto.getSecondName());
        persona.setLastname(dto.getLastName());
        persona.setSecondLastname(dto.getSecondLastName());
        persona.setGender(dto.getGender());
        persona.setPhone(dto.getMobile());
        persona.setEmail(dto.getEmail());

        personaRepository.save(persona);

        return true;
    }

    public boolean deletePersona(Integer id) {
        if (!personaRepository.existsById(id)) {
            return false;
        }

        personaRepository.deleteById(id);
        return true;
    }


}
