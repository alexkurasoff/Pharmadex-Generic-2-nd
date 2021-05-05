package org.msh.pharmadex.mbean.amendment;

import java.util.Date;

import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.AmendmentSubject;
import org.msh.pharmadex.domain.enums.PharmadexDB;

/**
 * This DAO class represents a row in any given amendment list table 
 * @author Alex Kurasoff
 *
 */
public class AmdListDTO {
	private Long id;
	private Date amdDate;
	private PharmadexDB pharmDB;
	private AmendmentSubject subject;
	private AmendmentProcess process;
	private AmdState state;
	private String prodName;
	
	public AmdListDTO(Long id, Date amdDate, PharmadexDB pharmDB, AmendmentSubject subject, AmendmentProcess process,
			AmdState state, String prod_name) {
		super();
		this.id = id;
		this.amdDate = amdDate;
		this.pharmDB = pharmDB;
		this.subject = subject;
		this.process = process;
		this.state = state;
		this.prodName = prod_name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAmdDate() {
		return amdDate;
	}
	public void setAmdDate(Date amdDate) {
		this.amdDate = amdDate;
	}
	public PharmadexDB getPharmDB() {
		return pharmDB;
	}
	public void setPharmDB(PharmadexDB amdType) {
		this.pharmDB = amdType;
	}
	public AmendmentSubject getSubject() {
		return subject;
	}
	public void setSubject(AmendmentSubject subject) {
		this.subject = subject;
	}
	public AmdState getState() {
		return state;
	}
	public void setState(AmdState state) {
		this.state = state;
	}

	public AmendmentProcess getProcess() {
		return process;
	}

	public void setProcess(AmendmentProcess process) {
		this.process = process;
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	
}
