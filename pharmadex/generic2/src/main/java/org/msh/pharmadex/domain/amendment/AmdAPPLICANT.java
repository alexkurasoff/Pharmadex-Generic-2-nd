package org.msh.pharmadex.domain.amendment;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
/**
 * Specific Amendment for Applicant's data
 * @author Alex Kurasoff
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdAPPLICANT")
public class AmdAPPLICANT extends Amendment {
	private static final long serialVersionUID = 5487516649666856487L;

	@Column(name="`Name`", nullable=true, length=50)	
	private String applName;

	@Column(name="`LicNo`", nullable=true, length=32)	
	private String licNo;

	public void setApplName(String value) {
		this.applName = value;
	}

	public String getApplName() {
		return applName;
	}

	public void setLicNo(String value) {
		this.licNo = value;
	}

	public String getLicNo() {
		return licNo;
	}

	public String toString() {
		return super.toString();
	}

}
