package org.msh.pharmadex.mbean;

import java.util.Date;

import org.msh.pharmadex.domain.enums.InvoiceState;

/**
 * This DAO class represents a row in any given invoices list table 
 * 
 */
public class InvoiceListDTO {
	private Long id;
	private Date issueDate;
	private InvoiceState state;
	private String applicantName;
	
	public InvoiceListDTO(Long id, Date issueDate, InvoiceState state, String applicantName) {
		super();
		this.id = id;
		this.issueDate = issueDate;
		this.state = state;
		this.applicantName = applicantName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getApplicantName() {
		return applicantName;
	}

	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	public InvoiceState getState() {
		return state;
	}

	public void setState(InvoiceState state) {
		this.state = state;
	}

}
