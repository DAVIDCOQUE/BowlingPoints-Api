package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeEventRepository extends JpaRepository<TipoEvento,Integer> {


}
