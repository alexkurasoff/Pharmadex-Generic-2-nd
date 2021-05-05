package org.msh.pharmadex.domain;

import org.msh.pharmadex.domain.amendment.AmdDocuments;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.PayType;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.ReviewStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Author: usrivastava
 */
@Entity
//@Audited
public class ProdApplications extends CreationDetail implements Serializable {
    private static final long serialVersionUID = 3054470055191648660L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    //@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY) 20161017 AK
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ID", nullable = false)
    private Applicant applicant;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROD_ID", nullable = true)
    private Product product;

    @Column(length = 255)
    private String prodAppNo;

    @Column(length = 255)
    private String prodRegNo;

    @Column(length = 500)
    private String appComment;

    @Enumerated(EnumType.STRING)
    private ProdAppType prodAppType;
    
  	@Embedded
    @AttributeOverrides({    	 
    	@AttributeOverride(name = "payment_form",column = @Column(name="payType")),    	
    	@AttributeOverride(name = "feeSum",column = @Column(name="feeSum")),
    	@AttributeOverride(name = "payment_receipt",column = @Column(name="receiptNo")), 
    	@AttributeOverride(name = "payment_payDate",column = @Column(name="feeSubmittedDt")),
    	@AttributeOverride(name = "payment_received",column = @Column(name="feeReceived"))
    	
    })	
    private FeePayment payment = new FeePayment();
       	
	@Embedded
    @AttributeOverrides({    	
    	@AttributeOverride(name = "payment_form",column = @Column(name="prescreenPayType")) ,     	 
    	@AttributeOverride(name = "feeSum",column = @Column(name="prescreenFeeSum")),
    	@AttributeOverride(name = "payment_receipt",column = @Column(name="prescreenReceiptNo")), 
    	@AttributeOverride(name = "payment_payDate",column = @Column(name="prescreenfeeSubmittedDt")),
    	@AttributeOverride(name = "payment_received",column = @Column(name="prescreenfeeReceived"))
    })	
    private FeePayment paymentPrescreen = new FeePayment();
	
 
    @Column(length = 500)
    private String prescreenBankName;
    
    @Column(length = 255)
    private String feeAmt;

    @Column(length = 255)
    private String prescreenfeeAmt;

    @Lob
    @Column(nullable = true)
    private byte[] feeReceipt;

    @OneToOne
    private Attachment cRevAttach;

    @Lob
    @Column(nullable = true)
    private byte[] clinicalReview;

    @Enumerated(EnumType.STRING)
    private RegState regState;

    @Temporal(TemporalType.DATE)
    private Date submitDate;

    @Temporal(TemporalType.DATE)
    private Date lastStatusDate;

    @Temporal(TemporalType.DATE)
    private Date registrationDate;

    @Temporal(TemporalType.DATE)
    private Date regExpiryDate;

    @Temporal(TemporalType.DATE)
    private Date archivingDate;

    private boolean active;

    private boolean dccApproval;

    private boolean clinicalRevReceived;

    private boolean dossierReceived;

    private Boolean sampleTestRecieved;

    @Temporal(TemporalType.DATE)
    private Date dosRecDate;

    private boolean applicantVerified;

    private boolean productVerified;

    private boolean clinicalRevVerified;

    private boolean sra;

    private boolean fastrack;
    
    private String prodSrcNo;
  /**
   * @deprecated
   */
    @OneToMany(mappedBy = "prodApplications")
    private List<ProdAppAmdmt> prodAppAmdmts;
    
    @OneToOne(mappedBy = "prodApplications")
    private Amendment amendment;

    @OneToMany(mappedBy = "prodApplications", cascade = {CascadeType.ALL})
    private List<ReviewInfo> reviewInfos;

    public ProdApplications getParentApplication() {
		return parentApplication;
	}


	public void setParentApplication(ProdApplications parentApplication) {
		this.parentApplication = parentApplication;
	}

	@OneToMany(mappedBy = "prodApplications", cascade = {CascadeType.ALL})
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MODERATOR_ID", nullable = true)
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicantUser")
    private User applicantUser;

    @OneToOne(cascade = CascadeType.ALL)
    private Appointment appointment;

    @Column(length = 500)
    private String dossLoc;

    @Lob
    @Column(name = "exec_summary")
    private String execSummary;

    private boolean sendToGazette;

    @Lob
    @Column(nullable = true)
    private byte[] regCert;

    @Lob
    @Column(nullable = true)
    private byte[] rejCert;

    @Column(length = 255)
    private String username;

    @Column(length = 255)
    private String position;

    @Transient
    private ReviewStatus reviewStatus;

    @Column(length = 255, name = "priorityNo")
    private String priorityNo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date priorityDate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "papp_ID", nullable = true)
    private ProdApplications parentApplication;

    @Column(nullable = true, columnDefinition = "int default 0")
    private int mjVarQnt = 0;

    @Column(nullable = true, columnDefinition = "int default 0")
    private int mnVarQnt = 0;

    @Column(nullable = true)
    private String prodAppDetails;

    public ProdApplications(Product prod, Applicant applicant) {
        this.product = prod;
        this.applicant = applicant;
    }

   /**
    * Amendment that is source of this application
    * @return
    */
	public Amendment getAmendment() {
		return amendment;
	}

	/**
	 * Amendment that is source of this application
	 * @param amendment
	 */
	public void setAmendment(Amendment amendment) {
		this.amendment = amendment;
	}


	public ProdApplications(Product prod) {
        this.product = prod;
    }

    public ProdApplications() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public String getProdAppNo() {
        return prodAppNo;
    }

    public void setProdAppNo(String prodAppNo) {
        this.prodAppNo = prodAppNo;
    }

    public String getAppComment() {
        return appComment;
    }

    public void setAppComment(String appComment) {
        this.appComment = appComment;
    }

    public ProdAppType getProdAppType() {
        return prodAppType;
    }

    public void setProdAppType(ProdAppType prodAppType) {
        this.prodAppType = prodAppType;
    }
    
    public FeePayment getPayment() {
		return payment;
	}

	public void setPayment(FeePayment payment) {
		this.payment = payment;
	}
	
	public FeePayment getPaymentPrescreen() {
		return paymentPrescreen;
	}

	public void setPaymentPrescreen(FeePayment paymentPrescreen) {
		this.paymentPrescreen = paymentPrescreen;
	}
	
	public String getPrescreenBankName() {
        return prescreenBankName;
    }

    public void setPrescreenBankName(String prescreenBankName) {
        this.prescreenBankName = prescreenBankName;
    }
  
	public String getFeeAmt() {
        return feeAmt;
    }

    public void setFeeAmt(String feeAmt) {
        this.feeAmt = feeAmt;
    }

    public String getPrescreenfeeAmt() {
        return prescreenfeeAmt;
    }

    public void setPrescreenfeeAmt(String prescreenfeeAmt) {
        this.prescreenfeeAmt = prescreenfeeAmt;
    }

    public RegState getRegState() {
        return regState;
    }

    public void setRegState(RegState regState) {
        this.regState = regState;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Date getLastStatusDate() {
        return lastStatusDate;
    }

    public void setLastStatusDate(Date lastStatusDate) {
        this.lastStatusDate = lastStatusDate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getRegExpiryDate() {
        return regExpiryDate;
    }

    public void setRegExpiryDate(Date regExpiryDate) {
        this.regExpiryDate = regExpiryDate;
    }
    
    public boolean isDossierReceived() {
        return dossierReceived;
    }

    public void setDossierReceived(boolean dossierReceived) {
        this.dossierReceived = dossierReceived;
    }

    public boolean isApplicantVerified() {
        return applicantVerified;
    }

    public void setApplicantVerified(boolean applicantVerified) {
        this.applicantVerified = applicantVerified;
    }

    public boolean isProductVerified() {
        return productVerified;
    }

    public void setProductVerified(boolean productVerified) {
        this.productVerified = productVerified;
    }

    public boolean isSra() {
        return sra;
    }

    public void setSra(boolean sra) {
        this.sra = sra;
    }

    public boolean isFastrack() {
        return fastrack;
    }

    public void setFastrack(boolean fastrack) {
        this.fastrack = fastrack;
    }

    public List<ProdAppAmdmt> getProdAppAmdmts() {
        return prodAppAmdmts;
    }

    public void setProdAppAmdmts(List<ProdAppAmdmt> prodAppAmdmts) {
        this.prodAppAmdmts = prodAppAmdmts;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getDossLoc() {
        return dossLoc;
    }

    public void setDossLoc(String dossLoc) {
        this.dossLoc = dossLoc;
    }

    public boolean isSendToGazette() {
        return sendToGazette;
    }

    public void setSendToGazette(boolean sendToGazette) {
        this.sendToGazette = sendToGazette;
    }

    public byte[] getRegCert() {
        return regCert;
    }

    public void setRegCert(byte[] regCert) {
        this.regCert = regCert;
    }

    public byte[] getRejCert() {
        return rejCert;
    }

    public void setRejCert(byte[] rejCert) {
        this.rejCert = rejCert;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getProdRegNo() {
        return prodRegNo;
    }

    public void setProdRegNo(String prodRegNo) {
        this.prodRegNo = prodRegNo;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getApplicantUser() {
        return applicantUser;
    }

    public void setApplicantUser(User applicantUser) {
        this.applicantUser = applicantUser;
    }

    public List<ReviewInfo> getReviewInfos() {
        return reviewInfos;
    }

    public void setReviewInfos(List<ReviewInfo> reviewInfos) {
        this.reviewInfos = reviewInfos;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getExecSummary() {
        return execSummary;
    }

    public void setExecSummary(String execSummary) {
        this.execSummary = execSummary;
    }

    public ReviewStatus getReviewStatus() {
        if(regState.equals(RegState.REVIEW_BOARD)){
        	reviewInfos = this.getReviewInfos();
            if(reviewInfos==null)
                reviewStatus = ReviewStatus.NOT_ASSIGNED;
            else if(reviewInfos.size()<1)
                reviewStatus = ReviewStatus.NOT_ASSIGNED;
            else {
                reviewStatus = ReviewStatus.NOT_ASSIGNED;
                for(ReviewInfo reviewInfo : reviewInfos){
                    if (reviewStatus.ordinal() < reviewInfo.getReviewStatus().ordinal())
                        reviewStatus = reviewInfo.getReviewStatus();
                }
            }

        }else{
            reviewStatus = null;
        }
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Date getDosRecDate() {
        return dosRecDate;
    }

    public void setDosRecDate(Date dosRecDate) {
        this.dosRecDate = dosRecDate;
    }

    public byte[] getFeeReceipt() {
        return feeReceipt;
    }

    public void setFeeReceipt(byte[] feeReceipt) {
        this.feeReceipt = feeReceipt;
    }

    public Attachment getcRevAttach() {
        return cRevAttach;
    }

    public void setcRevAttach(Attachment cRevAttach) {
        this.cRevAttach = cRevAttach;
    }

    public byte[] getClinicalReview() {
        return clinicalReview;
    }

    public void setClinicalReview(byte[] clinicalReview) {
        this.clinicalReview = clinicalReview;
    }

    public boolean isClinicalRevReceived() {
        return clinicalRevReceived;
    }

    public void setClinicalRevReceived(boolean clinicalRevReceived) {
        this.clinicalRevReceived = clinicalRevReceived;
    }

    public boolean isClinicalRevVerified() {
        return clinicalRevVerified;
    }

    public void setClinicalRevVerified(boolean clinicalRevVerified) {
        this.clinicalRevVerified = clinicalRevVerified;
    }

    @Override
    public String toString() {
        return "" + id;
    }

    public String getPriorityNo() {
        return priorityNo;
    }

    public void setPriorityNo(String priorityNo) {
        this.priorityNo = priorityNo;
    }

    public boolean isDccApproval() {
        return dccApproval;
    }

    public void setDccApproval(boolean dccApproval) {
        this.dccApproval = dccApproval;
    }

    public Boolean getSampleTestRecieved() {
        return sampleTestRecieved;
    }

    public void setSampleTestRecieved(Boolean sampleTestRecieved) {
        this.sampleTestRecieved = sampleTestRecieved;
    }

    public Date getPriorityDate() {
        return priorityDate;
    }

    public void setPriorityDate(Date priorityDate) {
        this.priorityDate = priorityDate;
    }

    public Date getArchivingDate() {
        return archivingDate;
    }

    public void setArchivingDate(Date archivingDate) {
        this.archivingDate = archivingDate;
    }

    public int getMjVarQnt() {
        return mjVarQnt;
    }

    public void setMjVarQnt(int mjVarQnt) {
        this.mjVarQnt = mjVarQnt;
    }

    public int getMnVarQnt() {
        return mnVarQnt;
    }

    public void setMnVarQnt(int mnVarQnt) {
        this.mnVarQnt = mnVarQnt;
    }

    public String getProdAppDetails() {
        return prodAppDetails;
    }

    public void setProdAppDetails(String prodAppDetails) {
        this.prodAppDetails = prodAppDetails;
    }


	public String getProdSrcNo() {
		return prodSrcNo;
	}

	public void setProdSrcNo(String prodSrcNo) {
		this.prodSrcNo = prodSrcNo;
	}

	//20/09/17
	/**
	 * The application number is screening number. It is strictly before application fee will be confirmed
	 * @return
	 */
	/*--
	@Transient
	public boolean isScreeningNum(){
		return !getPayment().isPayment_received();
	}
	--*/
	/**
	 * This application has registration number
	 * @return
	 */
	/*--
	@Transient	
	private boolean hasRegNum() {
		return getProdRegNo() != null && getProdRegNo().length()>4;
	}
	--*/

	/**
	 * This application has application number
	 * @return
	 */
	//20/09/17
	/*--
	@Transient	
	private boolean hasApplNum() {
		return getProdAppNo() != null && getProdAppNo().length()>5;
	}
	--/

	/**
	 * This application has screening number
	 * @return
	 */
	//20/09/17
		/*--
	@Transient
	private boolean hasScreeningNum() {
		return getProdSrcNo() != null && getProdSrcNo().length()>5;
	}
	--*/
	/**
	 * Return application number if it is allowed
	 * @return
	 */
	//20/09/17
	/*--
	 @Transient
	public String getActualAppNum(){
		if(isScreeningNum()){
			return "";
		}else{
			return getProdAppNo();
		}
	}
	--*/

	
}

