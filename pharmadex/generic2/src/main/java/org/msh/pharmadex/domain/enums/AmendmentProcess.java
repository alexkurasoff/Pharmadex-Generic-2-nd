package org.msh.pharmadex.domain.enums;

/**
 * How to process this amendment
 * @author Alex Kurasoff
 *
 */
public enum AmendmentProcess {
	NOTIFICATION,
	APPROVAL,
	NEW_APPLICATION;
	
	 public String getKey() {
	        return getClass().getSimpleName().concat("." + name());
	    }
	
}
