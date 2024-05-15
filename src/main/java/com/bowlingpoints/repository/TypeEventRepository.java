package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TypeEvent;
import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeEventRepository extends JpaRepository<TypeEvent,Integer> {


}
