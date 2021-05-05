package org.msh.pharmadex.domain.amendment;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
/**
 * Specific Amendment to change product ADMIN ROUTE
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdSHELF_LIFE")
public class AmdSHELF_LIFE extends Amendment {

	private static final long serialVersionUID = -9001135347582620329L;
	
	@Column(name="`Name`", nullable=true, length=100)	
	private String shelfLife;
	
	public String getShelfLife() {
		return shelfLife;
	}

	public void setShelfLife(String shelfLife) {
		this.shelfLife = shelfLife;
	}

	public String toString() {
		return super.toString();
	}
}
