package org.msh.pharmadex.domain.amendment;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
/**
 * Specific Amendment to change product description and physical appearance
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdSHAPE")
public class AmdSHAPE extends Amendment {
	private static final long serialVersionUID = -111863732620146796L;

    @Column(name = "prod_desc", length = 4096)
    private String prodDesc;
		
    public String getProdDesc() {
		return prodDesc;
	}

	public void setProdDesc(String prodDesc) {
		this.prodDesc = prodDesc;
	}
	
	public String toString() {
		return super.toString();
	}
}
