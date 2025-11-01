package com.bowlingpoints.dto;

public interface UserStatsProjection {

    Integer getTotalTournaments();

    Integer getTotalStrikes();

    Double getAvgScore();

    Integer getBestGame();

    Integer getTournamentsWon();
}