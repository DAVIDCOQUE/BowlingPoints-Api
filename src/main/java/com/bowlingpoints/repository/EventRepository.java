package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event,Integer> {


}
