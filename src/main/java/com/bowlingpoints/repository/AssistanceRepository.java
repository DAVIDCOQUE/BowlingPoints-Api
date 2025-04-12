package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Asistencia;
import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistanceRepository extends JpaRepository<Asistencia,Integer> {


}
