package org.msh.pharmadex.domain.enums;

/**
 * Subject for an amendment
 * @author Alex Kurasoff
 *
 */
public enum AmendmentSubject {
	APPLICANT,
	PRODUCT,
	API,
	IPI;
	
	 public String getKey() {
	        return getClass().getSimpleName().concat("." + name());
	    }
}
