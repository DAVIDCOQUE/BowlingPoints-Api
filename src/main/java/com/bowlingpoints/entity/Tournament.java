package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "tournament")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    private int tournamentId;

    private String tournamentName;

    private Boolean status;

    private Integer createdBy;

    private Date startDate;

    private Date endDate;

    private Date createdAt;

    private Integer updatedBy;

    private Date updatedAt;

}
