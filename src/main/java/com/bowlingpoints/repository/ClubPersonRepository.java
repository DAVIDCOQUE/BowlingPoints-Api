package com.bowlingpoints.repository;

import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClubPersonRepository extends JpaRepository<ClubPerson, Integer> {

    List<ClubPerson> findByClubAndStatusIsTrue(Clubs club);

    List<ClubPerson> findByPersonAndStatusIsTrue(Person person);

    @Transactional
    void deleteAllByClub_ClubId(Integer clubId);
}
