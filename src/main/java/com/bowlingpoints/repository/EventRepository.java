package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Evento,Integer> {

    List<Evento> findByIdTipoEvento(int tipoEvento);

    List<Evento> findByIdEstadoEvento(int estadoEvento);

    List<Evento> findByIdEstadoEventoAndIdTipoEvento(int idEstadoEvento, int idTipoEvento);

    Evento findById(int idEvento);


}
