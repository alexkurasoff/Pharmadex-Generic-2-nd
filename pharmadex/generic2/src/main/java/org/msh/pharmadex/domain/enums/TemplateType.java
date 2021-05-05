package org.msh.pharmadex.domain.enums;

/**
 * Types of file templates. Each template has own type
 * @author Alex Kurasoff
 *
 */
public enum TemplateType {
	DOSSIER_RECIEPT,
	DEFICIENCY,
	CERTIFICATE,
	REJECTION,
	SCREENING_NOTIFICATION,
	SCREENING_OUTCOME,
	RETENTION;
	
	public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }
}
