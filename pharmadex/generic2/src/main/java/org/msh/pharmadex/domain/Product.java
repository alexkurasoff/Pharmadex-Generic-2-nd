package org.msh.pharmadex.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_NAME;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.AgeGroup;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.ProdDrugType;
import org.msh.pharmadex.domain.enums.ProdType;
import org.msh.pharmadex.domain.enums.UseCategory;
import org.msh.pharmadex.domain.enums.YesNoNA;

@Entity
@Table(name = "product")
@Audited
public class Product extends CreationDetail implements Serializable {
	private static final long serialVersionUID = -8204053633675277911L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true)
	private Long id;

	@Column(name = "prod_name", length = 500)
	@Size(max = 500, min = 3)
	private String prodName;

	@Column(name = "gen_name", length = 500)
	@Size(max = 500, min = 3)
	private String genName;

	@Column(name = "apprvd_name", length = 500)
	@Size(max = 500, min = 3)
	private String apprvdName;

	@Column(name = "prod_desc", length = 4096)
	private String prodDesc;

	@Column(name = "new_chemical_entity")
	private boolean newChemicalEntity;

	@Column(name = "new_chem_name", length = 500)
	@Size(max = 500, min = 3)
	private String newChemicalName;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOSFORM_ID")
	@NotAudited
	private DosageForm dosForm;

	@Column(name = "dosage_strength")
	private String dosStrength;

	//@ManyToOne(fetch = FetchType.LAZY) 20161017 AK
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOSUNIT_ID")
	@NotAudited
	private DosUom dosUnit;

	@Column(name = "prod_type")
	@Enumerated(EnumType.STRING)
	@NotAudited
	private ProdType prodType;

	@Column(name = "age_group")
	@Enumerated(EnumType.STRING)
	private AgeGroup ageGroup;

	@Column(name = "prod_cat")
	@Enumerated(EnumType.STRING)
	private ProdCategory prodCategory;

	@OneToMany(mappedBy = "product", cascade = {CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@NotAudited
	private List<ProdInn> inns;

	//@OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY) 20161017 AK
	@OneToMany(mappedBy = "product", cascade = {CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@NotAudited
	private List<ProdExcipient> excipients;

	//@ManyToMany(targetEntity = Atc.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL}) 20161017 AK
	@ManyToMany(targetEntity = Atc.class, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinTable(name = "prod_atc", joinColumns = @JoinColumn(name = "prod_id"), inverseJoinColumns = @JoinColumn(name = "atc_id"))
	@NotAudited
	private List<Atc> atcs;

	private boolean noAtc = false;

	@ManyToMany(targetEntity = PcCode.class, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinTable(name = "prod_pccode", joinColumns = @JoinColumn(name = "prod_id"), inverseJoinColumns = @JoinColumn(name = "pccode_id"))
	@NotAudited
	private List<PcCode> pcCodes;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ADMIN_ROUTE_ID")
	@NotAudited
	private AdminRoute adminRoute;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PHARM_CLASSIF_ID")
	@NotAudited
	private PharmClassif pharmClassif;

	@OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@NotAudited
	private List<ProdCompany> prodCompanies;

	@OneToMany(mappedBy = "product")
	@NotAudited
	private List<ProdApplications> prodApplicationses;

	@Column(name = "manuf_name", length = 1000)
	private String manufName;

	@Column(length = 500)
	private String ingrdStatment;

	//@OneToOne(cascade = CascadeType.ALL)
	@OneToOne(cascade=CascadeType.ALL)
	@NotAudited
	private Pricing pricing;

	@Enumerated(EnumType.STRING)
	private ProdDrugType drugType;

	@ElementCollection(targetClass = UseCategory.class)
	@JoinTable(name = "tblusecategories", joinColumns = @JoinColumn(name = "prodID"))
	@Column(name = "useCategory")
	@Enumerated(EnumType.STRING)
	private List<UseCategory> useCategories;

	@Column(length = 500)
	private String packSize;

	@Column(length = 500)
	private String contType;

	@Column(length = 500)
	private String shelfLife;

	@Column(length = 500)
	private String storageCndtn;

	@Column(length = 4096)
	private String indications;

	@Column(length = 4096)
	private String posology;

	@Column(length = 500)
	@Enumerated(EnumType.STRING)
	private YesNoNA fnm;

	@Column(length = 500)
	private String pharmacopeiaStds;


	@OneToMany(mappedBy = "product", targetEntity=Amendment.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
	private List<AmdPRODUCT_NAME> amendment;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}

	public String getGenName() {
		return genName;
	}

	public void setGenName(String genName) {
		this.genName = genName;
	}

	public String getApprvdName() {
		return apprvdName;
	}

	public void setApprvdName(String apprvdName) {
		this.apprvdName = apprvdName;
	}

	public String getProdDesc() {
		return prodDesc;
	}

	public void setProdDesc(String prodDesc) {
		this.prodDesc = prodDesc;
	}

	public boolean isNewChemicalEntity() {
		return newChemicalEntity;
	}

	public void setNewChemicalEntity(boolean newChemicalEntity) {
		this.newChemicalEntity = newChemicalEntity;
	}

	public String getNewChemicalName() {
		return newChemicalName;
	}

	public void setNewChemicalName(String newChemicalName) {
		this.newChemicalName = newChemicalName;
	}

	public DosageForm getDosForm() {
		return dosForm;
	}

	public void setDosForm(DosageForm dosForm) {
		this.dosForm = dosForm;
	}

	public String getDosStrength() {
		return dosStrength;
	}

	public void setDosStrength(String dosStrength) {
		this.dosStrength = dosStrength;
	}

	public DosUom getDosUnit() {
		return dosUnit;
	}

	public void setDosUnit(DosUom dosUnit) {
		this.dosUnit = dosUnit;
	}

	public ProdType getProdType() {
		return prodType;
	}

	public void setProdType(ProdType prodType) {
		this.prodType = prodType;
	}

	public AgeGroup getAgeGroup() {
		return ageGroup;
	}

	public void setAgeGroup(AgeGroup ageGroup) {
		this.ageGroup = ageGroup;
	}

	public ProdCategory getProdCategory() {
		return prodCategory;
	}

	public void setProdCategory(ProdCategory prodCategory) {
		this.prodCategory = prodCategory;
	}

	public List<ProdInn> getInns() {
		return inns;
	}

	public void setInns(List<ProdInn> inns) {
		this.inns = inns;
	}

	public List<ProdExcipient> getExcipients() {
		return excipients;
	}

	public void setExcipients(List<ProdExcipient> excipients) {
		this.excipients = excipients;
	}

	public List<Atc> getAtcs() {
		return atcs;
	}

	public void setAtcs(List<Atc> atcs) {
		this.atcs = atcs;
	}

	public boolean isNoAtc() {
		return noAtc;
	}

	public void setNoAtc(boolean noAtc) {
		this.noAtc = noAtc;
	}

	public AdminRoute getAdminRoute() {
		return adminRoute;
	}

	public void setAdminRoute(AdminRoute adminRoute) {
		this.adminRoute = adminRoute;
	}

	public PharmClassif getPharmClassif() {
		return pharmClassif;
	}

	public void setPharmClassif(PharmClassif pharmClassif) {
		this.pharmClassif = pharmClassif;
	}

	public List<ProdCompany> getProdCompanies() {
		return prodCompanies;
	}

	public void setProdCompanies(List<ProdCompany> prodCompanies) {
		this.prodCompanies = prodCompanies;
	}

	public String getIngrdStatment() {
		return ingrdStatment;
	}

	public void setIngrdStatment(String ingrdStatment) {
		this.ingrdStatment = ingrdStatment;
	}

	public Pricing getPricing() {
		return pricing;
	}

	public void setPricing(Pricing pricing) {
		this.pricing = pricing;
	}

	public ProdDrugType getDrugType() {
		return drugType;
	}

	public void setDrugType(ProdDrugType drugType) {
		this.drugType = drugType;
	}

	public List<UseCategory> getUseCategories() {
		return useCategories;
	}

	public void setUseCategories(List<UseCategory> useCategories) {
		this.useCategories = useCategories;
	}

	public String getPackSize() {
		return packSize;
	}

	public void setPackSize(String packSize) {
		this.packSize = packSize;
	}

	public String getContType() {
		return contType;
	}

	public void setContType(String contType) {
		this.contType = contType;
	}

	public String getShelfLife() {
		return shelfLife;
	}

	public void setShelfLife(String shelfLife) {
		this.shelfLife = shelfLife;
	}

	public String getStorageCndtn() {
		return storageCndtn;
	}

	public void setStorageCndtn(String storageCndtn) {
		this.storageCndtn = storageCndtn;
	}

	public String getIndications() {
		return indications;
	}

	public void setIndications(String indications) {
		this.indications = indications;
	}

	public String getPosology() {
		return posology;
	}

	public void setPosology(String posology) {
		this.posology = posology;
	}

	public List<ProdApplications> getProdApplicationses() {
		return prodApplicationses;
	}

	public void setProdApplicationses(List<ProdApplications> prodApplicationses) {
		this.prodApplicationses = prodApplicationses;
	}

	public YesNoNA getFnm() {
		return fnm;
	}

	public void setFnm(YesNoNA fnm) {
		this.fnm = fnm;
	}

	public String getPharmacopeiaStds() {
		return pharmacopeiaStds;
	}

	public void setPharmacopeiaStds(String pharmacopeiaStds) {
		this.pharmacopeiaStds = pharmacopeiaStds;
	}

	public String getManufName() {
		return manufName;
	}

	public void setManufName(String manufName) {
		this.manufName = manufName;
	}

	@Override
	public String toString() {
		return getProdName();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public List<AmdPRODUCT_NAME> getAmendment() {
		return amendment;
	}

	public void setAmendment(List<AmdPRODUCT_NAME> amendment) {
		this.amendment = amendment;
	}

	public List<PcCode> getPcCodes() {
		return pcCodes;
	}

	public void setPcCodes(List<PcCode> pcCodes) {
		this.pcCodes = pcCodes;
	}

	@Transient
	public String getDosStrengthUnit(){
		if(getDosUnit() != null){
			return getDosStrength() + "/" + getDosUnit();
		}else{
			return getDosStrength();
		}
	}
	@Transient
	public void setDosStrengthUnit(String dummy){
		// nothing to do
	}

}

