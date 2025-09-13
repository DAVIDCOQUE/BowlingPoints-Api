package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResultUploadDTO {
    private String document;
    private String fullName;
    private String club;
    private Set<Integer> roundsLoaded;
    private int totalLines;
}
