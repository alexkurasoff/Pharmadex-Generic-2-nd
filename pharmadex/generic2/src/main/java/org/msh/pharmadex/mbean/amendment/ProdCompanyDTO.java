package org.msh.pharmadex.mbean.amendment;

import org.msh.pharmadex.domain.ProdCompany;

/**
 * This DAO class represents a Manufacturer in Amendment form
 * @author dudchenko
 *
 */
public class ProdCompanyDTO {
	
	private ProdCompany prodCompany;
	private boolean delete = false;
	
	public ProdCompanyDTO(ProdCompany prodComp) {
		super();
		this.prodCompany = prodComp;
		this.delete = false;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public ProdCompany getProdCompany() {
		return prodCompany;
	}

	public void setProdCompany(ProdCompany prodCompany) {
		this.prodCompany = prodCompany;
	}
	
	
}
