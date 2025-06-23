package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TeamPerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamPersonRepository extends JpaRepository<TeamPerson, Integer> {
    void deleteAllByTeam_TeamId(Integer teamId);
}
