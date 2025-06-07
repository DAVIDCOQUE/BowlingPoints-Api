package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Registry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<Registry,Integer> {


}
