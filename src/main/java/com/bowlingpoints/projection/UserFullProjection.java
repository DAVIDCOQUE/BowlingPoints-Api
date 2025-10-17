package com.bowlingpoints.projection;

import java.time.LocalDate;

/**
 * Proyección de datos completos de usuario con su información personal y rol principal.
 */
public interface UserFullProjection {
    Integer getUserId();
    Integer getPersonId();
    String getPhotoUrl();
    String getNickname();
    String getDocument();
    String getEmail();
    String getFullName();
    String getFullSurname();
    LocalDate getBirthDate();
    String getPhone();
    String getGender();
    String getRoleName();
}
