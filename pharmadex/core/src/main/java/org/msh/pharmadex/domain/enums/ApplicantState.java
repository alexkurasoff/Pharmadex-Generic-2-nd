package org.msh.pharmadex.domain.enums;

/**
 * Author: usrivastava
 */
public enum ApplicantState {
    NEW_APPLICATION,
    REGISTERED,
    BLOCKED,
    REGISTRATION_EXPIRED,
    SUSPENDED;

    public String getKey() {
   		return getClass().getSimpleName().concat("." + name());
   	}


}
