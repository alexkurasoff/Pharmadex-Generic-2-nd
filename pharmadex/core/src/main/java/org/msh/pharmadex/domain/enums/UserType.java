package org.msh.pharmadex.domain.enums;

public enum UserType {
    STAFF,
    COMPANY,
    INSPECTOR,
    TIPC,
    EXTERNAL;

    public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }

}
