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

import org.msh.pharmadex.domain.ProdCompany;
/**
 * Specific Amendment to remove manuf
 * 
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdREMOVE_MANUF")
public class AmdREMOVE_MANUF extends Amendment {

	private static final long serialVersionUID = 5345887056926014902L;
	
	@ManyToMany(targetEntity = ProdCompany.class, fetch = FetchType.LAZY)
    @JoinTable(name = "amendment_prodCompany", joinColumns = @JoinColumn(name = "amd_id"), inverseJoinColumns = @JoinColumn(name = "prodComp_id"))
    private List<ProdCompany> prodCompany;
	
	public String toString() {
		return super.toString();
	}

	public List<ProdCompany> getProdCompany() {
		return prodCompany;
	}

	public void setProdCompany(List<ProdCompany> list) {
		this.prodCompany = list;
	}
}
