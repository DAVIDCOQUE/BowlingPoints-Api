package com.bowlingpoints.repository;

import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistanceRepository extends JpaRepository<User,Integer> {


}
