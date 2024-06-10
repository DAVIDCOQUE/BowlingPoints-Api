package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<Registro,Integer> {


}
