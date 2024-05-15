package com.bowlingpoints.repository;

import com.bowlingpoints.entity.EventEntity;
import com.bowlingpoints.entity.TypeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity,Integer> {


}
