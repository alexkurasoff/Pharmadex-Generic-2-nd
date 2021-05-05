package org.msh.pharmadex.domain.enums;

public enum InvoiceState {
	SAVED,
    SUBMITTED,   
    APPROVED,
    REJECTED;

    public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }

}
