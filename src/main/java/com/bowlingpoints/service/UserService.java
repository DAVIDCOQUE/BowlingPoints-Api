package com.bowlingpoints.service;

import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.TipoEvento;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.TypeEventRepository;
import com.bowlingpoints.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public String listUserPersons(){

        List<User> userList = userRepository.findAll();


        return null;
    }

}
