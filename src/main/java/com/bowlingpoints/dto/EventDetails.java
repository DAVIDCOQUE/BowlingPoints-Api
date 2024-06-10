package com.bowlingpoints.dto;


import com.bowlingpoints.entity.Rama;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetails {

    private String nameEvent;
    private String descriptionEvent;
    private String eventOrganizer;
    private Rama branches;




}
