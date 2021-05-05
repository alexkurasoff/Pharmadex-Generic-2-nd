package org.msh.pharmadex.domain.amendment;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.msh.pharmadex.domain.DosageForm;
/**
 * Specific Amendment to change product DOSFORM
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdPRODUCT_DOSFORM")
public class AmdPRODUCT_DOSFORM extends Amendment {

	private static final long serialVersionUID = -9067537018853327278L;
	
	@OneToOne(targetEntity=DosageForm.class, fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	@JoinColumn(name="`DosageFormID`", nullable = true)
    private DosageForm dosageForm;
	
	public DosageForm getDosageForm() {
		return dosageForm;
	}

	public void setDosageForm(DosageForm dosageForm) {
		this.dosageForm = dosageForm;
	}
	
	public String toString() {
		return super.toString();
	}
}
