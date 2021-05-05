package org.msh.pharmadex.service;

import java.util.Date;

import org.msh.pharmadex.domain.enums.ProdAppType;

public class HomeDTO {
	
	private String proprietaryName;
	private ProdAppType prodAppType;
	private String prodSrcNo;
	private String prodAppNo;
	private Long prodAppId;
	private Date date;
	
	public HomeDTO(){
		super();
	}
	
	public HomeDTO(String proprietaryName, ProdAppType prodAppType,String prodSrcNo, String prodAppNo, Long prodAppId, Date date) {
		super();
		this.proprietaryName = proprietaryName;
		this.prodAppType = prodAppType;
		this.prodSrcNo  = prodSrcNo;
		this.prodAppNo  = prodAppNo;
		this.prodAppId = prodAppId;
		this.date  = date;
	}

	public String getProprietaryName() {
		return proprietaryName;
	}

	public void setProprietaryName(String proprietaryName) {
		this.proprietaryName = proprietaryName;
	}

		
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public Long getProdAppId() {
		return prodAppId;
	}

	public void setProdAppId(Long prodAppId) {
		this.prodAppId = prodAppId;
	}

	public ProdAppType getProdAppType() {
		return prodAppType;
	}

	public void setProdAppType(ProdAppType prodAppType) {
		this.prodAppType = prodAppType;
	}

	public String getProdAppNo() {
		return prodAppNo;
	}

	public void setProdAppNo(String prodAppNo) {
		this.prodAppNo = prodAppNo;
	}

	public String getProdSrcNo() {
		return prodSrcNo;
	}

	public void setProdSrcNo(String prodSrcNo) {
		this.prodSrcNo = prodSrcNo;
	}

}
