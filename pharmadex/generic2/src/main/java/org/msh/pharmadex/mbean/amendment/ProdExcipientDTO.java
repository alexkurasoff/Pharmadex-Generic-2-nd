package org.msh.pharmadex.mbean.amendment;

import org.msh.pharmadex.domain.ProdExcipient;

/**
 * This DAO class represents a ProdExcipient in Amendment form
 * @author dudchenko
 *
 */
public class ProdExcipientDTO {
	
	private ProdExcipient prodExc = null;
	private boolean delete = false;
	
	public ProdExcipientDTO(ProdExcipient prodEx) {
		super();
		this.prodExc = prodEx;
		this.delete = false;
	}

	public ProdExcipient getProdExc() {
		return prodExc;
	}

	public void setProdExc(ProdExcipient prodExc) {
		this.prodExc = prodExc;
	}
	
	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
}
