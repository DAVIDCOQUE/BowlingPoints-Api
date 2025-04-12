package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Evento;
import com.bowlingpoints.entity.EventoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoCategoriaRepository extends JpaRepository<EventoCategoria,Integer> {

    List<EventoCategoria> findByIdEvento(int idEvento);
}
