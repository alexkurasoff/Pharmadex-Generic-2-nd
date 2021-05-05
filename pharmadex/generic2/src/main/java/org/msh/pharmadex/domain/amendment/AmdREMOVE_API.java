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

import org.msh.pharmadex.domain.ProdInn;
/**
 * Specific Amendment to remove API
 * 
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdREMOVE_API")
public class AmdREMOVE_API extends Amendment {

	private static final long serialVersionUID = -7916332095962163117L;
	
	@ManyToMany(targetEntity = ProdInn.class, fetch = FetchType.LAZY)
    @JoinTable(name = "amendment_prodInn", joinColumns = @JoinColumn(name = "amd_id"), inverseJoinColumns = @JoinColumn(name = "prodInn_id"))
    private List<ProdInn> prodInn;
	
	public String toString() {
		return super.toString();
	}

	public List<ProdInn> getProdInn() {
		return prodInn;
	}

	public void setProdInn(List<ProdInn> prodInn) {
		this.prodInn = prodInn;
	}
}
