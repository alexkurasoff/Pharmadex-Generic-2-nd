package org.msh.pharmadex.domain;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.pharmadex.domain.enums.InvoiceState;

@Entity
@Table(name = "invoice")
public class Invoice extends CreationDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
    private Long id;
   
    @Temporal(TemporalType.DATE)
    @Column(name = "issue_date", nullable = true)
    private Date issueDate=null;

    @Column(name = "invoice_number", length = 255)
    private String invoiceNumber;
    
    @Column(name = "curYear", length = 4)
    private String curYear;

    /*@Column(name = "invoice_amt", length = 50, nullable = false)
    private Long invoiceAmt;*/
       
    @ManyToOne(targetEntity=Applicant.class, fetch=FetchType.LAZY)
	@JoinColumn(name = "applcntId", nullable = false)
	public Applicant applicant;

    @Lob
    @Column(nullable = true)
    private byte[] invoiceFile;
    
    @Column(name="`State`", nullable=true, length=32)
	@Enumerated(EnumType.STRING)
	private InvoiceState state;
    
    @Column(name="`ApprDate`", nullable=true)	
	private java.util.Date apprDate;
	
	@Column(name="`RejectDate`", nullable=true)	
	private java.util.Date rejectDate;
	
	@Temporal(TemporalType.DATE)
	private Date regExpiryDate;
	
	@Embedded
    @AttributeOverrides({    	 
    	@AttributeOverride(name = "payment_form",column = @Column(name="payType")),    	
    	@AttributeOverride(name = "feeSum",column = @Column(name="feeSum")),
    	@AttributeOverride(name = "payment_receipt",column = @Column(name="receiptNo")), 
    	@AttributeOverride(name = "payment_payDate",column = @Column(name="feeSubmittedDt")),
    	@AttributeOverride(name = "payment_received",column = @Column(name="feeReceived"))
    	
    })	
    private FeePayment payment = new FeePayment();
		
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public byte[] getInvoiceFile() {
        return invoiceFile;
    }

    public void setInvoiceFile(byte[] invoiceFile) {
        this.invoiceFile = invoiceFile;
    }

	public FeePayment getPayment() {
		return payment;
	}

	public void setPayment(FeePayment payment) {
		this.payment = payment;
	}
/*
	public Long getInvoiceAmt() {
		return invoiceAmt;
	}

	public void setInvoiceAmt(Long invoiceAmt) {
		this.invoiceAmt = invoiceAmt;
	}
*/
	public InvoiceState getState() {
		return state;
	}

	public void setState(InvoiceState state) {
		this.state = state;
	}

	public Applicant getApplicant() {
		return applicant;
	}

	public void setApplicant(Applicant applicant) {
		this.applicant = applicant;
	}

	public String getCurYear() {
		return curYear;
	}

	public void setCurYear(String curYear) {
		this.curYear = curYear;
	}

	public java.util.Date getApprDate() {
		return apprDate;
	}

	public void setApprDate(java.util.Date apprDate) {
		this.apprDate = apprDate;
	}

	public java.util.Date getRejectDate() {
		return rejectDate;
	}

	public void setRejectDate(java.util.Date rejectDate) {
		this.rejectDate = rejectDate;
	}

	public Date getRegExpiryDate() {
		return regExpiryDate;
	}

	public void setRegExpiryDate(Date regExpiryDate) {
		this.regExpiryDate = regExpiryDate;
	}
		
}
