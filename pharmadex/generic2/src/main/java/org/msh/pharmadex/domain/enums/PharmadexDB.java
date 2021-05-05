package org.msh.pharmadex.domain.enums;

/**
 * Parts of Pharmadex Database that may be amended
 * @author Alex Kurasoff
 *
 */
public enum PharmadexDB {
	NOT_APPLICABLE,
	APPLICANT,
	PRODUCT_NAME,
	SHAPE,
    PRODUCT_DOSFORM,
    PRODUCT_ADMINROUTE,
    ADD_IPI,
    REMOVE_IPI,
    REPLACEMENT_IPI,
    ADD_API,
    REMOVE_API,
    EDIT_API,
    SHELF_LIFE,
    REMOVE_MANUF,
    ADD_MANUF;
	
	//TODO the rest from Best Practice
	 public String getKey() {
        return getClass().getSimpleName().concat("." + name());
    }
	
	/** get name MBean */
	public String getMBeanName(){
		return "amd" + name() + "MBean";
	}
	
	/** get name template by summary tab */
	public String getSummaryTemplate(){
		return "/templates/amendment/amd" + name() + "summary.xhtml";
	}
	
	/** get name template by detail tab */
	public String getDetaileTemplate(){
		return "/templates/amendment/amd" + name() + "detail.xhtml";
	}
}
