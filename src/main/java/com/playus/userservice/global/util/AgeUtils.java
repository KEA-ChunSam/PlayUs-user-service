package com.playus.userservice.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class AgeUtils {

    // 생년월일로부터 실제 나이를 계산
    public static int calculateAge(LocalDateTime birthDateTime) {
        LocalDate birthDate = birthDateTime.toLocalDate();
        LocalDate today     = LocalDate.now();
        return Period.between(birthDate, today).getYears();
    }

    //나이를 10단위로 내림해서 연령대로 반환
    public static int calculateAgeGroup(LocalDateTime birthDateTime) {
        int age = calculateAge(birthDateTime);
        return (age / 10) * 10;
    }
}
