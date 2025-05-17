package com.playus.userservice.domain.user.enums;

import java.util.Arrays;

public enum Team {

    NC_DINOS(1, "NC Dinos"),
    SAMSUNG_LIONS(2, "Samsung Lions"),
    DOOSAN_BEARS(3, "Doosan Bears"),
    HANWHA_EAGLES(4, "Hanwha Eagles"),
    KIA_TIGERS(5, "KIA Tigers"),
    KT_WIZ(6, "KT Wiz"),
    LOTTE_GIANTS(7, "Lotte Giants"),
    LG_TWINS(8, "LG Twins"),
    SSG_LANDERS(9, "SSG Landers"),
    KIWOOM_HEROES(10, "Kiwoom Heroes");

    private final long id;
    private final String displayName;

    Team(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public long id() { return id; }
    public String displayName() { return displayName; }

    // id → Enum 매핑
    public static Team fromId(long id) {
        return Arrays.stream(values())
                .filter(t -> t.id == id)
                .findFirst()
                .orElse(null);
    }
}