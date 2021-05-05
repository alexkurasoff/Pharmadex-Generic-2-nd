package org.msh.pharmadex.domain.amendment;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.msh.pharmadex.domain.ProdExcipient;
/**
 * Specific Amendment to remove IPI
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdREMOVE_IPI")
public class AmdREMOVE_IPI extends Amendment {
	
	private static final long serialVersionUID = -828250450469643968L;

	@ManyToMany(targetEntity = ProdExcipient.class, fetch = FetchType.LAZY)
    @JoinTable(name = "amendment_prodExcipient", joinColumns = @JoinColumn(name = "amd_id"), inverseJoinColumns = @JoinColumn(name = "prodExc_id"))
    private List<ProdExcipient> prodExcipient;
	
	public String toString() {
		return super.toString();
	}

	public List<ProdExcipient> getProdExcipient() {
		return prodExcipient;
	}

	public void setProdExcipient(List<ProdExcipient> prodExcipient) {
		this.prodExcipient = prodExcipient;
	}
	
}
