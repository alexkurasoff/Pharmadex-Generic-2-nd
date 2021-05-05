package org.msh.pharmadex.domain.amendment;

import javax.persistence.*;
/**
 * Specific Amendment to change product proprietary name
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdPRODUCT_NAME")
public class AmdPRODUCT_NAME extends Amendment {
	private static final long serialVersionUID = -111863732620146796L;
	@Column(name="`Name`", nullable=true, length=50)	
	private String propName;
	
	public void setPropName(String value) {
		this.propName = value;
	}
	
	public String getPropName() {
		return propName;
	}
	
	public String toString() {
		return super.toString();
	}
}
