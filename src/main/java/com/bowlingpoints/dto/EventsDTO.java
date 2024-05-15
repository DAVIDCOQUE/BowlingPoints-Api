package com.bowlingpoints.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventsDTO {

    private String nameEvent;
    private String descriptionEvent;
    private String eventOrganizer;

}
