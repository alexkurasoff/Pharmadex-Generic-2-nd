package org.msh.pharmadex.mbean.amendment;

import org.msh.pharmadex.domain.ProdInn;

/**
 * This DAO class represents a ProdInn in Amendment form
 * @author dudchenko
 *
 */
public class ProdInnDTO {
	
	private ProdInn prodInn = null;
	private boolean delete = false;
	
	public ProdInnDTO(ProdInn prod) {
		super();
		this.prodInn = prod;
		this.delete = false;
	}

	public ProdInn getProdInn() {
		return prodInn;
	}

	public void setProdExc(ProdInn prodI) {
		this.prodInn = prodI;
	}
	
	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
}
