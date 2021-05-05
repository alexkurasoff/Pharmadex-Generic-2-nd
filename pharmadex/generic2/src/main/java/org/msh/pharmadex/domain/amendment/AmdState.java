package org.msh.pharmadex.domain.amendment;

/**
 * Author: usrivastava
 */
public enum AmdState {
    SAVED,
    SUBMITTED,
    RECOMMENDED,
    NOT_RECOMMENDED,
    APPROVED,
    REJECTED;


    public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }

}
