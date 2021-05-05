package org.msh.pharmadex.domain.enums;

/**
 * Created by usrivastava on 05/22/2015.
 */
public enum SuspensionStatus {
    REQUESTED,
    ASSIGNED,
    IN_PROGRESS,
    SUBMIT,
    FEEDBACK,
    RESULT,
    COMPLETED,
    ARCHIVE;

    public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }
}
