package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Register;
import com.bowlingpoints.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<Register,Integer> {


}
