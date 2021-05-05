package org.msh.pharmadex.domain.amendment;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.msh.pharmadex.domain.Address;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.CreationDetail;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.AmendmentSubject;
import org.msh.pharmadex.domain.enums.PharmadexDB;
/**
 * General amendment - comments only
 * Suits for common amendments and works as common ancestor for specific amendments
 * @author Alex Kurasoff
 *
 */
@Entity
@Table(name="Amendment")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="Discriminator", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("Amendment")
public class Amendment extends CreationDetail implements Serializable{

	private static final long serialVersionUID = 4906750596828631027L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
    private Long id;
	
	@OneToOne(targetEntity=User.class, fetch=FetchType.LAZY)		
	@JoinColumn(name = "USEREXEC_ID", nullable = true)		
	private User executor;
	
	@OneToOne(targetEntity=ProdApplications.class, fetch=FetchType.LAZY)		
	@JoinColumn(name = "PA_ID", nullable = true)	
	private ProdApplications prodApplications;
	
	@ManyToOne(targetEntity=Applicant.class, fetch=FetchType.LAZY)
	@JoinColumn(name = "applcntId", nullable = false)
	public Applicant applicant;
	
	@OneToOne(targetEntity=Product.class, fetch=FetchType.LAZY)		
	@JoinColumn(name = "PROD_ID", nullable = true)
	private Product product;
	
	@OneToOne(targetEntity=AmendmentDictionary.class, fetch=FetchType.LAZY)	
	@JoinColumn(name="`AmendmentDictionaryId`", nullable=true)		
	private AmendmentDictionary dictionary;
	
	@OneToMany(mappedBy="amendment", targetEntity=AmdConditions.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)		
	private List<AmdConditions> conditions;
	
	@OneToMany(mappedBy="amendment", targetEntity=AmdDocuments.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)		
	private List<AmdDocuments> documents;
	
	@OneToMany(mappedBy="amendment", targetEntity=ProdAppChecklist.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)		
	private List<ProdAppChecklist> prodAppChecklist;
	
	@Column(name="`AppComment`", nullable=true, length=512)	
	private String appComment;
	
	@Column(name="`StaffComment`", nullable=true, length=512)	
	private String staffComment;
	
	@Column(name="`RegistrarComment`", nullable=true, length=512)	
	private String registrarComment;
	
	@Column(name="`ApprDate`", nullable=true)	
	private java.util.Date apprDate;
	
	@Column(name="`RejectDate`", nullable=true)	
	private java.util.Date rejectDate;
	
	@Column(name="`EffectiveDate`", nullable=true)	
	private java.util.Date effectiveDate;
	
	@Column(name="`State`", nullable=true, length=32)
	@Enumerated(EnumType.STRING)
	private AmdState state;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AmendmentDictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(AmendmentDictionary dictionary) {
		this.dictionary = dictionary;
	}

	public List<AmdConditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<AmdConditions> conditions) {
		this.conditions = conditions;
	}

	public List<AmdDocuments> getDocuments() {
		return documents;
	}

	public void setDocuments(List<AmdDocuments> documents) {
		this.documents = documents;
	}

	public List<ProdAppChecklist> getProdAppChecklist() {
		return prodAppChecklist;
	}

	public void setProdAppChecklist(List<ProdAppChecklist> prodAppChecklist) {
		this.prodAppChecklist = prodAppChecklist;
	}

	public void setAppComment(String value) {
		this.appComment = value;
	}
	
	public String getAppComment() {
		return appComment;
	}
	
	public void setStaffComment(String value) {
		this.staffComment = value;
	}
	
	public String getStaffComment() {
		return staffComment;
	}
	
	public void setRegistrarComment(String value) {
		this.registrarComment = value;
	}
	
	public String getRegistrarComment() {
		return registrarComment;
	}
	
	/**
	 * real Approve Date
	 * @param value
	 */
	public void setApprDate(java.util.Date value) {
		this.apprDate = value;
	}
	
	/**
	 * real Approve Date
	 * @param value
	 */
	public java.util.Date getApprDate() {
		return apprDate;
	}
	
	public void setRejectDate(java.util.Date value) {
		this.rejectDate = value;
	}
	
	public java.util.Date getRejectDate() {
		return rejectDate;
	}
	
	/**
	 * add change in DB
	 * @param value
	 */
	public void setEffectiveDate(java.util.Date value) {
		this.effectiveDate = value;
	}
	
	/**
	 * add change in DB
	 * @param value
	 */
	public java.util.Date getEffectiveDate() {
		return effectiveDate;
	}
	
	public void setState(AmdState value) {
		this.state = value;
	}
	
	public AmdState getState() {
		return state;
	}
	
	
	public void setProdApplications(ProdApplications value) {
		this.prodApplications = value;
	}
	
	public ProdApplications getProdApplications() {
		return prodApplications;
	}
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Applicant getApplicant() {
		return applicant;
	}

	public void setApplicant(Applicant applicant) {
		this.applicant = applicant;
	}

	public void setExecutor(User value) {
		this.executor = value;
	}
	
	public User getExecutor() {
		return executor;
	}
	
	@Embedded	
	private Address address;
	
	public Address getAddress()  {
		return this.address;
	}
	
	public void setAddress(Address value)  {
		this.address = value;
	}
	
	@Embedded	
	private Telecom telecom;
	
	public Telecom getTelecom()  {
		return this.telecom;
	}
	
	public void setTelecom(Telecom value)  {
		this.telecom = value;
	}
	
	@Embedded	
	private FeePayment payment;
	
	public FeePayment getPayment()  {
		return this.payment;
	}
	
	public void setPayment(FeePayment value)  {
		this.payment = value;
	}
	
	@Transient
	public PharmadexDB getPharmadexDB(){
		if(getDictionary() != null)
			return getDictionary().getPharmadexDB();
		return null;
	}
	
	@Transient
	public AmendmentSubject getAmdSubject(){
		if(getDictionary() != null)
			return getDictionary().getSubject();
		return null;
	}

	@Transient
	public void setPropName(String productName) {
		throw new UnsupportedOperationException("getAmdSubject must be implemented by Super Class");
	}
	
	@Transient
	public String getPropName() {
		throw new UnsupportedOperationException("getAmdSubject must be implemented by Super Class");
	}

	@Transient
	public void setProdDesc(String prodDesc) {
		throw new UnsupportedOperationException("getAmdSubject must be implemented by Super Class");
	}
	
	@Transient
	public String getProdDesc() {
		throw new UnsupportedOperationException("getAmdSubject must be implemented by Super Class");
	}
	
}
