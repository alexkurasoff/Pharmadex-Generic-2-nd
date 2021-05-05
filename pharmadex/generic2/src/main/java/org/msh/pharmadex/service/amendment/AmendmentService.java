package org.msh.pharmadex.service.amendment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hibernate.Hibernate;
import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.dao.ApplicantDAO;
import org.msh.pharmadex.dao.ExcipientDAO;
import org.msh.pharmadex.dao.InnDAO;
import org.msh.pharmadex.dao.ProductDAO;
import org.msh.pharmadex.dao.UserDAO;
import org.msh.pharmadex.dao.iface.AdminRouteDAO;
import org.msh.pharmadex.dao.iface.AmdADD_APIDAO;
import org.msh.pharmadex.dao.iface.AmdADD_IPIDAO;
import org.msh.pharmadex.dao.iface.AmdADD_MANUFDAO;
import org.msh.pharmadex.dao.iface.AmdAPPLICANTDAO;
import org.msh.pharmadex.dao.iface.AmdEDIT_APIDAO;
import org.msh.pharmadex.dao.iface.AmdPRODUCT_ADMINROUTEDAO;
import org.msh.pharmadex.dao.iface.AmdPRODUCT_DOSFORMDAO;
import org.msh.pharmadex.dao.iface.AmdPRODUCT_NAMEDAO;
import org.msh.pharmadex.dao.iface.AmdREMOVE_APIDAO;
import org.msh.pharmadex.dao.iface.AmdREMOVE_IPIDAO;
import org.msh.pharmadex.dao.iface.AmdREMOVE_MANUFDAO;
import org.msh.pharmadex.dao.iface.AmdREPLACEMENT_IPIDAO;
import org.msh.pharmadex.dao.iface.AmdSHAPEDAO;
import org.msh.pharmadex.dao.iface.AmdSHELF_LIFEDAO;
import org.msh.pharmadex.dao.iface.AmendmentDAO;
import org.msh.pharmadex.dao.iface.ChecklistDAO;
import org.msh.pharmadex.dao.iface.CompanyDAO;
import org.msh.pharmadex.dao.iface.DosageFormDAO;
import org.msh.pharmadex.dao.iface.ProdAppChecklistDAO;
import org.msh.pharmadex.dao.iface.ProdCompanyDAO;
import org.msh.pharmadex.dao.iface.ProdExcipientDAO;
import org.msh.pharmadex.dao.iface.ProdInnDAO;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.Address;
import org.msh.pharmadex.domain.AdminRoute;
import org.msh.pharmadex.domain.AgentAgreement;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.Checklist;
import org.msh.pharmadex.domain.Company;
import org.msh.pharmadex.domain.Country;
import org.msh.pharmadex.domain.DosageForm;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.Mail;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.ProdExcipient;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.amendment.AmdADD_API;
import org.msh.pharmadex.domain.amendment.AmdADD_IPI;
import org.msh.pharmadex.domain.amendment.AmdADD_MANUF;
import org.msh.pharmadex.domain.amendment.AmdAPPLICANT;
import org.msh.pharmadex.domain.amendment.AmdConditions;
import org.msh.pharmadex.domain.amendment.AmdDocuments;
import org.msh.pharmadex.domain.amendment.AmdEDIT_API;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_ADMINROUTE;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_DOSFORM;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_NAME;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_API;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_IPI;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_MANUF;
import org.msh.pharmadex.domain.amendment.AmdREPLACEMENT_IPI;
import org.msh.pharmadex.domain.amendment.AmdSHAPE;
import org.msh.pharmadex.domain.amendment.AmdSHELF_LIFE;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.CompanyType;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.mbean.amendment.AmdListDTO;
import org.msh.pharmadex.mbean.amendment.ProdCompanyDTO;
import org.msh.pharmadex.mbean.amendment.ProdExcipientDTO;
import org.msh.pharmadex.mbean.amendment.ProdInnDTO;
import org.msh.pharmadex.service.ApplicantService;
import org.msh.pharmadex.service.MailService;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common business logic for all amendment processing steps
 * In addition contains very specific database related codes
 * @author Alex Kurasoff
 *
 */
@Service
public class AmendmentService {
	private static final String AMD_ORDERBY_CREATEDATE = " order by amd.createdDate desc";

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	ApplicantDAO applicantDAO;

	@Autowired
	ApplicantService applicantService;

	@Autowired
	UserDAO userDAO;

	@Autowired
	AmdAPPLICANTDAO amdAPPLICANTDAO;

	@Autowired
	AmdPRODUCT_NAMEDAO amdPRODUCT_NAMEDAO;

	@Autowired
	AmdADD_MANUFDAO amdADD_MANUFDAO;

	@Autowired
	AmdREMOVE_MANUFDAO amdREMOVE_MANUFDAO;

	@Autowired
	AmendmentDAO amendmentDAO;

	@Autowired
	AmdSHAPEDAO amdSHAPEDAO;

	@Autowired
	AmdPRODUCT_ADMINROUTEDAO amdPRODUCT_ADMINROUTEDAO;

	@Autowired
	AmdSHELF_LIFEDAO amdSHELF_LIFEDAO;

	@Autowired
	AmdPRODUCT_DOSFORMDAO amdPRODUCT_DOSFORMDAO;

	@Autowired
	AmdADD_IPIDAO amdADD_IPIDAO;

	@Autowired
	AmdREMOVE_IPIDAO amdREMOVE_IPIDAO;

	@Autowired
	AmdREPLACEMENT_IPIDAO amdREPLACEMENT_IPIDAO;

	@Autowired
	AmdADD_APIDAO amdADD_APIDAO;

	@Autowired
	AmdREMOVE_APIDAO amdREMOVE_APIDAO;

	@Autowired
	AmdEDIT_APIDAO amdEDIT_APIDAO;

	@Autowired
	ProdApplicationsService prodApplicationsService;

	@Autowired
	ProductService productService;

	@Autowired
	ProductDAO productDAO;

	@Autowired
	CompanyDAO companyDAO;

	@Autowired
	ProdInnDAO prodInnDAO;

	@Autowired
	ProdExcipientDAO prodExcipientDAO;

	@Autowired
	ExcipientDAO excipientDAO;

	@Autowired
	ProdCompanyDAO prodCompanyDAO;

	@Autowired
	InnDAO innDAO;

	@Autowired
	ChecklistDAO checklistDAO;

	@Autowired
	ProdAppChecklistDAO prodAppChecklistDAO;

	@Autowired
	MailService mailService;

	@Autowired
	WorkspaceDAO workspaceDAO;

	@Autowired
	private DosageFormDAO dosageFormDAO;

	@Autowired
	private AdminRouteDAO adminRouteDAO;

	public EntityManager getEntityManager() {
		return entityManager;
	}
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Fetch product from the userSession
	 * @param userSession
	 */
	public List<Product> fetchProduct(Amendment amd) {
		if(amd != null){
			List<ProdApplications> paList = applicantService.findRegProductForApplicant(amd.getApplicant().getApplcntId());
			List<Product> products = new ArrayList<Product>();
			if(paList != null){
				for(ProdApplications pa : paList){
					products.add(pa.getProduct());
				}

				Hibernate.initialize(products);
			}
			return products;
		}
		return null;
	}

	/**
	 * Fetch responsible applicant's user
	 * @return
	 */
	public User fetchResponsibleUser(Applicant applicant) {
		if(applicant != null){
			List<User> users = applicant.getUsers();
			if(users != null){
				for(User u  :users){
					if(u.getUsername().equals(applicant.getContactName())){
						return u;
					}
				}
				return null;
			}
			return null;
		}
		return null;
	}

	public List<AmdDocuments> fetchAmdDocumentByAmendment(Long amdID){
		List<AmdDocuments> result = null;
		Amendment amd = findAmendmentById(amdID);
		if(amd != null){
			Hibernate.initialize(amd.getDocuments());
			result = amd.getDocuments();
		}
		return result;
	}

	public List<AmdConditions> fetchAmdConditionByAmendment(Long amdID){
		List<AmdConditions> result = null;
		Amendment amd = findAmendmentById(amdID);
		if(amd != null){
			Hibernate.initialize(amd.getConditions());
			result = amd.getConditions();
		}
		return result;
	}

	/**
	 * list only AmdAPPLICANT by show in applicant form
	 * Convert amendments to DTO for on screen tables
	 * @param appl
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<AmdListDTO> fetchAmendmentsByApplicant(Applicant appl) {
		List<AmdListDTO> result = new ArrayList<AmdListDTO>();

		String hql = "SELECT amd FROM Amendment amd "
				+ " left join amd.applicant as appl "
				+ " WHERE TYPE(amd) like 'AmdAPPLICANT'" 
				+ " and amd.state=:st"
				+ " and appl.id = " + appl.getApplcntId();
		TypedQuery<Amendment> query = getEntityManager().createQuery(hql, Amendment.class);
		List<Amendment> list = query.setParameter("st", AmdState.APPROVED).getResultList();

		if(list != null){
			for(Amendment amd : list){
				String prName = "";
				if(amd.getProduct() != null)
					prName = amd.getProduct().getProdName();
				result.add(new AmdListDTO(amd.getId(), amd.getUpdatedDate(), amd.getPharmadexDB(), amd.getAmdSubject(), 
						amd.getDictionary().getProcess(), amd.getState(), prName));
			}
		}

		return result;
	}

	/**
	 * Fetch all amendments that this user may submit<br>
	 * User can submit an amendment when the amendment is in SAVED state and is suit visible rules:
	 * @param user
	 * @param state 
	 * @return list of amendments or empty list and never null
	 */
	@Transactional(readOnly=true)
	public List<AmdListDTO> fetchAmendmentsByState(User user, AmdState state, UserSession userSession) {
		String sql = "SELECT amd FROM Amendment amd WHERE amd.state=:a_type";
		TypedQuery<Amendment> query = getEntityManager().createQuery(sql, Amendment.class);
		List<Amendment> amds = query.setParameter("a_type", state).getResultList();
		List<Amendment> byConvert = applyVisibleRulesToAmendments(user, amds, userSession);
		List<AmdListDTO> result = amendmentsToDTO(byConvert);
		return result;
	}

	/**
	 * list Amendment (no AmdAPPLICANT) by show in product form
	 * Convert amendments to DTO for on screen tables
	 * @param appl
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<AmdListDTO> fetchAmendmentsByProduct(Product prod) {
		List<AmdListDTO> result = new ArrayList<AmdListDTO>();

		String hql = "SELECT amd FROM Amendment amd "
				+ " left join amd.product as prod "
				+ " WHERE TYPE(amd) not like 'AmdAPPLICANT'"
				+ " and amd.state=:st"
				+ " and prod.id = " + prod.getId();
		TypedQuery<Amendment> query = getEntityManager().createQuery(hql, Amendment.class);
		List<Amendment> list = query.setParameter("st", AmdState.APPROVED).getResultList();

		if(list != null){
			for(Amendment amd : list){
				String prName = "";
				if(amd.getProduct() != null)
					prName = amd.getProduct().getProdName();

				result.add(new AmdListDTO(amd.getId(), amd.getUpdatedDate(), amd.getPharmadexDB(), amd.getAmdSubject(), 
						amd.getDictionary().getProcess(), amd.getState(), prName));
			}
		}
		return result;
	}

	public Company fetchCompany(long companyId){
		Company company = companyDAO.findOne(companyId);			
		return company;
	}

	/**
	 * get List ProdExcipient by product
	 * @param amd
	 * @return
	 */
	public List<ProdExcipient> fetchProdExcipients(Amendment amd) {
		List<ProdExcipient> result = new ArrayList<ProdExcipient>();
		if(amd != null && amd.getProduct() != null){
			result = prodExcipientDAO.findByProduct_Id(amd.getProduct().getId());
			if(result != null && result.size() > 0){
				for(ProdExcipient pe:result){
					Hibernate.initialize(pe);
					Hibernate.initialize(pe.getExcipient());
					Hibernate.initialize(pe.getDosUnit());
				}
			}
		}
		return result;
	}

	/**
	 * get List ProdInn by product
	 * @param amd
	 * @return
	 */
	public List<ProdInn> fetchProdInns(Amendment amd) {
		List<ProdInn> result = new ArrayList<ProdInn>();
		if(amd != null && amd.getProduct() != null){
			result = prodInnDAO.findByProduct_Id(amd.getProduct().getId());
			if(result != null && result.size() > 0){
				for(ProdInn pi:result){
					Hibernate.initialize(pi);
					Hibernate.initialize(pi.getDosUnit());
					Hibernate.initialize(pi.getCompany());
					Hibernate.initialize(pi.getCompany().getAddress());
				}
			}
		}
		return result;
	}

	/**
	 * get List ProdCompany by product
	 * @param amd
	 * @return
	 */
	public List<ProdCompany> fetchProdCompanies(AmdADD_MANUF amd) {
		List<ProdCompany> result = new ArrayList<ProdCompany>();
		if(amd != null && amd.getProduct() != null){
			result = prodCompanyDAO.findByProduct_Id(amd.getProduct().getId());
			if(result != null && result.size() > 0){
				for(ProdCompany pc:result){
					Hibernate.initialize(pc);
					Hibernate.initialize(pc.getCompany());
					Hibernate.initialize(pc.getCompany().getAddress());
					Hibernate.initialize(pc.getCompany().getAddress().getCountry());
				}
			}
		}

		return result;
	}

	/**
	 * get List ProdExcipientDTO by product
	 * @param amd
	 * @return
	 */
	public List<ProdExcipientDTO> fetchProdExcipientDTO(Amendment amd) {
		List<ProdExcipientDTO> result = new ArrayList<ProdExcipientDTO>();
		if(amd != null && amd.getProduct() != null){
			List<ProdExcipient> list = prodExcipientDAO.findByProduct_Id(amd.getProduct().getId());
			//List<ProdExcipient> delete = amd.getProdExcipient();

			if(list != null && list.size() > 0){
				for(ProdExcipient p:list){
					ProdExcipientDTO dto = new ProdExcipientDTO(p);
					if(p.getAmendment() != null && p.getAmendment().size() > 0){
						for(Amendment a:p.getAmendment()){
							if(a.getId() == amd.getId())
								dto.setDelete(true);
						}
					}
					
					Hibernate.initialize(dto.getProdExc());
					Hibernate.initialize(dto.getProdExc().getExcipient());
					
					result.add(dto);
				}
			}
		}
		return result;
	}

	/**
	 * get List ProdExcipientDTO by product
	 * @param amd
	 * @return
	 */
	public List<ProdInnDTO> fetchProdInnDTO(Amendment amd) {
		List<ProdInnDTO> result = new ArrayList<ProdInnDTO>();
		if(amd != null && amd.getProduct() != null){
			List<ProdInn> list = prodInnDAO.findByProduct_Id(amd.getProduct().getId());

			if(list != null && list.size() > 0){
				for(ProdInn p:list){
					ProdInnDTO dto = new ProdInnDTO(p);
					if(p.getAmendment() != null && p.getAmendment().size() > 0){
						for(Amendment a:p.getAmendment()){
							if(a.getId() == amd.getId())
								dto.setDelete(true);
						}
					}
					
					Hibernate.initialize(dto.getProdInn());
					Hibernate.initialize(dto.getProdInn().getInn());
					Hibernate.initialize(dto.getProdInn().getCompany());
					Hibernate.initialize(dto.getProdInn().getCompany().getAddress());
					Hibernate.initialize(dto.getProdInn().getCompany().getAddress().getCountry());
					
					result.add(dto);
				}
			}
		}
		return result;
	}

	/**
	 * get List ProdCompanyDTO by product
	 * @param amd
	 * @return
	 */
	public List<ProdCompanyDTO> fetchProdCompanyDTO(Amendment amd) {
		List<ProdCompanyDTO> result = new ArrayList<ProdCompanyDTO>();
		if(amd != null && amd.getProduct() != null){
			List<ProdCompany> list = prodCompanyDAO.findByProduct_Id(amd.getProduct().getId());

			if(list != null && list.size() > 0){
				for(ProdCompany p:list){
					ProdCompanyDTO dto = new ProdCompanyDTO(p);
					if(p.getAmendment() != null && p.getAmendment().size() > 0){
						for(Amendment a:p.getAmendment()){
							if(a.getId() == amd.getId())
								dto.setDelete(true);
						}
					}
					
					Hibernate.initialize(dto.getProdCompany());
					Hibernate.initialize(dto.getProdCompany().getCompany());
					Hibernate.initialize(dto.getProdCompany().getCompany().getAddress());
					Hibernate.initialize(dto.getProdCompany().getCompany().getAddress().getCountry());
					
					result.add(dto);
				}
			}
		}
		return result;
	}

	/**
	 * Submitted from company user's point of view are all amendments that in states SUBMITTED, RECOMMENDED, NOT RECOMMENDED
	 * Submitted from department user's point of view are all amendments that in state SUBMITTED
	 * @param loggedInUserObj
	 * @param isCompany 
	 * @return
	 */
	public List<AmdListDTO> fetchSubmittedAmds(UserSession userSession) {
		List<AmdState> states = new ArrayList<AmdState>();
		if(!userSession.isHead()){
			states.add(AmdState.SUBMITTED);
		}
		states.add(AmdState.RECOMMENDED);
		states.add(AmdState.NOT_RECOMMENDED);
		if(userSession.isCompany())
			states.add(AmdState.REJECTED);

		String hql = "SELECT amd FROM Amendment amd "
				+ " left join amd.dictionary as dct "
				+ " WHERE amd.state in (:a_states)"
				+ " and dct.process not like '" + AmendmentProcess.NEW_APPLICATION.name() + "'"
				+ AMD_ORDERBY_CREATEDATE;
		TypedQuery<Amendment> query = getEntityManager().createQuery(hql, Amendment.class);
		List<Amendment> list = query.setParameter("a_states", states).getResultList();
		List<Amendment> byConvert = applyVisibleRulesToAmendments(userSession.getLoggedInUserObj(), list, userSession);

		return amendmentsToDTO(byConvert);
	}

	/**
	 * Convert amendments to DTO for on screen tables
	 * @param amds
	 * @return
	 */
	private List<AmdListDTO> amendmentsToDTO(List<Amendment> amds) {
		List<AmdListDTO> ret = new ArrayList<AmdListDTO>();
		if(amds != null){
			for(Amendment amd : amds){
				String prName = "";
				if(amd.getProduct() != null)
					prName = amd.getProduct().getProdName();

				ret.add(new AmdListDTO(amd.getId(),amd.getUpdatedDate(), amd.getPharmadexDB(), amd.getAmdSubject(), 
						amd.getDictionary().getProcess(), amd.getState(), prName));
			}
		}
		return ret;
	}

	public ProdApplications fetchProdApplicationByProduct (Amendment amd){
		List<ProdApplications> prodApplications = new ArrayList<ProdApplications>();
		if(amd != null){
			if(amd.getProduct()!=null){
				if(prodApplicationsService!=null){
					prodApplications = prodApplicationsService.findProdApplicationByProduct(amd.getProduct().getId());
					if(prodApplications!=null && prodApplications.size()>0){
						return prodApplications.get(0);
					}
				}
			}
		}
		return null;		
	}

	/**
	 * get ProdAppChecklist 
	 * if amdId > 0 then get ProdAppChecklist by amendment
		or
		create new List ProdAppChecklist
	 * @param amdDictID
	 * @param amdId
	 * @return
	 */
	public List<ProdAppChecklist> fetchAmdProdAppChecklist(Amendment amd, User curUser){
		List<ProdAppChecklist> result = null;
		List<Checklist> checkList = checklistDAO.findAllByOrderByOrdAsc();
		if(checkList != null && checkList.size() > 0){
			result = new ArrayList<ProdAppChecklist>();
			for(Checklist item:checkList){
				ProdAppChecklist appCheck = new ProdAppChecklist();
				appCheck.setChecklist(item);
				appCheck.setCreatedDate(new Date());
				appCheck.setCreatedBy(curUser);
				if(amd != null)
					appCheck.setAmendment(amd);

				result.add(appCheck);
			}
		}
		return result;
	}

	/**
	 * Store the applicant to the amendment
	 * @param amendment
	 * @param applicant 
	 */
	public void storeApplicantToAmd(Amendment amendment, Applicant applicant) {
		if(amendment != null){
			if(amendment.getApplicant() == null){
				amendment.setApplicant(applicant);
				amendment.setAddress(null);
			}else if(!amendment.getApplicant().getApplcntId().equals(applicant.getApplcntId())){
				amendment.setApplicant(applicant);
				amendment.setAddress(null);
			}else
				amendment.setApplicant(applicant);
		}
	}
	/**
	 * Store the product to the amendment
	 * @param amendment
	 * @param product 
	 */
	public void storeProductToAmd(Amendment amendment, Product product) {
		if(amendment != null){
			if(amendment.getProduct() == null){
				amendment.setProduct(product);
			}else if(!amendment.getProduct().getId().equals(product.getId())){
				amendment.setProduct(product);					
			}else
				amendment.setProduct(product);
		}
	}

	/**
	 * Copy an applicant's address to an amendment
	 * @param address
	 * @param amd
	 */
	public void copyAddress(Address address, Amendment amd) {
		Address addr = new Address();
		amd.setAddress(addr);
		addr.setAddress1(address.getAddress1());
		addr.setAddress2(address.getAddress2());
		addr.setCountry(address.getCountry());
		addr.setZipcode(address.getZipcode());
	}

	/**
	 * Copy ProdExcipient to an amendment
	 * @param address
	 * @param amd
	 */
	public ProdExcipient copyProdExcipient(ProdExcipient prodExc) {
		ProdExcipient newProdExc = new ProdExcipient();
		newProdExc.setExcipient(prodExc.getExcipient());
		newProdExc.setDosStrength(prodExc.getDosStrength());
		newProdExc.setDosUnit(prodExc.getDosUnit());
		newProdExc.setFunction(prodExc.getFunction());
		newProdExc.setRefStd(prodExc.getRefStd());
		newProdExc.setCreatedDate(new Date());

		newProdExc.setProduct(null);

		return newProdExc;
	}

	/**
	 * Copy ProdInn to an amendment
	 * @param address
	 * @param amd
	 */
	public ProdInn copyProdInn(ProdInn prodInn) {
		ProdInn newProdInn = new ProdInn();
		newProdInn.setInn(prodInn.getInn());
		newProdInn.setDosStrength(prodInn.getDosStrength());
		newProdInn.setDosUnit(prodInn.getDosUnit());
		newProdInn.setFunction(prodInn.getFunction());
		newProdInn.setRefStd(prodInn.getRefStd());
		newProdInn.setCreatedDate(new Date());
		newProdInn.setCompany(prodInn.getCompany());

		newProdInn.setProduct(null);

		return newProdInn;
	}

	/**
	 * remove empty ProdExcipient
	 * where link to product is NULL
	 * and link to Amendment is NULL
	 */
	public void removeEmptyLinkProdExcipient(){
		String hql = "select pexc from ProdExcipient pexc "
				+ " where pexc.product is null";
		List<ProdExcipient> list = (List<ProdExcipient>) entityManager.createQuery(hql).getResultList();
		if(list != null && list.size() > 0){
			for(ProdExcipient pexc:list){
				if(!(pexc.getAmendment() != null && pexc.getAmendment().size() > 0))
					prodExcipientDAO.delete(pexc);
			}
		}
	}

	/**
	 * Filter amendment list based on user
	 * <ul>
	 * <li> RULE 1. this user is initiator (CreatedBy or UpdatedBy), regardless of user type, or
	 * <li> RULE 2. this user is responsible of the amendment's applicant, or
	 * <li> RULE 3. this user is responsible of a valid agent of amendmnet's applicant
	 * </ul>
	 * @param user
	 * @param saved
	 * @return
	 * 
	 */
	private List<Amendment> applyVisibleRulesToAmendments(User user, List<Amendment> saved, UserSession userSession) {
		List<Amendment> ret = new ArrayList<Amendment>();
		if(saved != null){
			for(Amendment amd : saved){
				//RULE1
				if(user.getUserId().equals(amd.getCreatedBy().getUserId()) || 
						user.getUserId().equals(amd.getUpdatedBy().getUserId())){
					ret.add(amd);
					continue;
				}
				//RULE2,3
				if(checkApplicantOrAgent(user, amd)){
					ret.add(amd);
					continue;
				}

				if(userSession.isReviewer() || userSession.isHead()){
					ret.add(amd);
					continue;
				}
			}
		}
		return ret;
	}

	/**
	 * RULE 2 and 3
	 * @param user
	 * @param amd
	 * @return
	 */
	private boolean checkApplicantOrAgent(User user, Amendment amd) {
		Applicant app = amd.getApplicant();
		//RULE 2
		List<User> users = app.getUsers();
		for(User u : users){
			if(u.getUserId().equals(user.getUserId())){
				return true;
			}
		}
		//RULE 3
		List<AgentAgreement> agents = amd.getApplicant().getAgents();
		if(agents!=null){
			for(AgentAgreement a : agents){
				if(a.isValid()){
					users = a.getAgent().getUsers();
					if(users != null){
						for(User u : users){
							if(u.getUserId().equals(user.getUserId())){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public Product findProduct(Long prodId) {
		return productDAO.findProduct(prodId);
	}
	public Applicant findApplicant(long id) {
		return applicantDAO.findApplicant(id);
	}

	/**
	 * Polimorfic search amendment by ID
	 * @param amdId
	 * @return amendment or null!
	 */
	@Transactional(readOnly=true)
	public Amendment findAmendmentById(Long amdId) {
		Amendment ret=null;
		String sql = "SELECT amd FROM Amendment amd WHERE amd.id=:a_id";
		TypedQuery<Amendment> query = getEntityManager().createQuery(sql, Amendment.class);
		List<Amendment> amendments = query.setParameter("a_id", amdId).getResultList();
		if(amendments != null && amendments.size()>0){
			ret = amendments.get(0);
			initialize(ret);
		}
		return ret;
	}
	/**
	 * Polimorfic search amendment by ID
	 * @param amdId
	 * @return amendment or null!
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
	public AmdADD_MANUF findAmdADD_MANUF(Long amdId) {
		AmdADD_MANUF ret = amdADD_MANUFDAO.findOne(amdId);
		initialize(ret);
		if(ret.getProdCompany() != null && ret.getProdCompany().size() > 0){
			Hibernate.initialize(ret.getProdCompany());
			for(ProdCompany pc:ret.getProdCompany()){
				Hibernate.initialize(pc);
				Hibernate.initialize(pc.getCompany());
				Hibernate.initialize(pc.getCompany().getAddress());
				Hibernate.initialize(pc.getCompany().getAddress().getCountry());
			}
		}
		return ret;
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
	public AmdREMOVE_MANUF findAmdREMOVE_MANUF(Long id){
		AmdREMOVE_MANUF amd = amdREMOVE_MANUFDAO.findOne(id);		
		initialize(amd);
		if(amd.getProdCompany() != null && amd.getProdCompany().size() > 0){
			Hibernate.initialize(amd.getProdCompany());
			for(ProdCompany pc:amd.getProdCompany()){
				Hibernate.initialize(pc);
				Hibernate.initialize(pc.getCompany());
				Hibernate.initialize(pc.getCompany().getAddress());
				Hibernate.initialize(pc.getCompany().getAddress().getCountry());
			}
		}
		return amd;
	}

	public AmdPRODUCT_NAME findAmdPRODUCT_NAME(Long id){
		AmdPRODUCT_NAME amd = amdPRODUCT_NAMEDAO.findOne(id);		
		initialize(amd);
		return amd;
	}

	public AmdAPPLICANT findAmdAPPLICANT(Long id){
		AmdAPPLICANT amd = amdAPPLICANTDAO.findOne(id);		
		initialize(amd);
		return amd;
	}

	public AmdSHAPE findAmdSHAPE(Long id) {
		AmdSHAPE amd = amdSHAPEDAO.findOne(id);
		initialize(amd);
		return amd;
	}

	public AmdPRODUCT_DOSFORM findAmdPRODUCT_DOSFORM(Long id) {
		AmdPRODUCT_DOSFORM amd = amdPRODUCT_DOSFORMDAO.findOne(id);
		initialize(amd);
		Hibernate.initialize(amd.getDosageForm());
		return amd;
	}

	public AmdPRODUCT_ADMINROUTE findAmdPRODUCT_ADMINROUTE(Long id) {
		AmdPRODUCT_ADMINROUTE amd = amdPRODUCT_ADMINROUTEDAO.findOne(id);
		initialize(amd);
		Hibernate.initialize(amd.getAdminRoute());
		return amd;
	}

	public AmdSHELF_LIFE findAmdSHELF_LIFE(Long id) {
		AmdSHELF_LIFE amd = amdSHELF_LIFEDAO.findOne(id);
		initialize(amd);
		return amd;
	}

	public AmdREPLACEMENT_IPI findAmdREPLACEMENT_IPI(Long id){
		AmdREPLACEMENT_IPI amd = amdREPLACEMENT_IPIDAO.findOne(id);		
		initialize(amd);
		if(amd.getProdExcipient() != null && amd.getProdExcipient().size() > 0){
			Hibernate.initialize(amd.getProdExcipient());
			for(ProdExcipient pe:amd.getProdExcipient()){
				Hibernate.initialize(pe);
				Hibernate.initialize(pe.getExcipient());
				Hibernate.initialize(pe.getDosUnit());
			}
		}
		return amd;
	}

	public AmdADD_IPI findAmdADD_IPI(Long id) {
		AmdADD_IPI amd = amdADD_IPIDAO.findOne(id);
		if(amd != null){
			initialize(amd);
			if(amd.getProdExcipient() != null && amd.getProdExcipient().size() > 0){
				Hibernate.initialize(amd.getProdExcipient());
				for(ProdExcipient pe:amd.getProdExcipient()){
					Hibernate.initialize(pe);
					Hibernate.initialize(pe.getExcipient());
					Hibernate.initialize(pe.getDosUnit());
				}
			}
		}
		return amd;
	}

	public AmdREMOVE_IPI findAmdREMOVE_IPI(Long id) {
		AmdREMOVE_IPI amd = amdREMOVE_IPIDAO.findOne(id);
		if(amd != null){
			initialize(amd);
			if(amd.getProdExcipient() != null && amd.getProdExcipient().size() > 0){
				Hibernate.initialize(amd.getProdExcipient());
				for(ProdExcipient pe:amd.getProdExcipient()){
					Hibernate.initialize(pe);
					Hibernate.initialize(pe.getExcipient());
					Hibernate.initialize(pe.getDosUnit());
				}
			}
		}
		return amd;
	}

	public AmdADD_API findAmdADD_API(Long id) {
		AmdADD_API amd = amdADD_APIDAO.findOne(id);
		if(amd != null){
			initialize(amd);
			if(amd.getProdInn() != null && amd.getProdInn().size() > 0){
				Hibernate.initialize(amd.getProdInn());
				for(ProdInn pi:amd.getProdInn()){
					Hibernate.initialize(pi);
					Hibernate.initialize(pi.getDosUnit());
					Hibernate.initialize(pi.getCompany());
					Hibernate.initialize(pi.getCompany().getAddress());
				}
			}
		}
		return amd;
	}

	public AmdREMOVE_API findAmdREMOVE_API(Long id) {
		AmdREMOVE_API amd = amdREMOVE_APIDAO.findOne(id);
		if(amd != null){
			initialize(amd);
			if(amd.getProdInn() != null && amd.getProdInn().size() > 0){
				Hibernate.initialize(amd.getProdInn());
				for(ProdInn pi:amd.getProdInn()){
					Hibernate.initialize(pi);
					Hibernate.initialize(pi.getDosUnit());
					Hibernate.initialize(pi.getCompany());
					Hibernate.initialize(pi.getCompany().getAddress());
				}
			}
		}
		return amd;
	}

	public AmdEDIT_API findAmdEDIT_API(Long id) {
		AmdEDIT_API amd = amdEDIT_APIDAO.findOne(id);
		if(amd != null){
			initialize(amd);
			if(amd.getProdInn() != null && amd.getProdInn().size() > 0){
				Hibernate.initialize(amd.getProdInn());
				for(int i = 0; i < amd.getProdInn().size(); i++){
					Hibernate.initialize(amd.getProdInn().get(i));
					Hibernate.initialize(amd.getProdInn().get(i).getDosUnit());
					Hibernate.initialize(amd.getProdInn().get(i).getCompany());
					Hibernate.initialize(amd.getProdInn().get(i).getCompany().getAddress());
				}
			}
		}
		return amd;
	}

	/**
	 * find findProdInn
	 * @param amd
	 * @param isProdNotNull if true - find getProduct() != null
	 * @return
	 */
	public ProdInn findProdInnEdit(AmdEDIT_API amd, boolean isProdNotNull){
		List<ProdInn> list = amd.getProdInn();
		if(list != null && list.size() == 2){
			if(isProdNotNull){
				if(list.get(0).getProduct() != null)
					return list.get(0);
				if(list.get(1).getProduct() != null)
					return list.get(1);	
			}else{
				if(list.get(0).getProduct() == null)
					return list.get(0);
				if(list.get(1).getProduct() == null)
					return list.get(1);	
			}
		}
		return null;
	}

	/**
	 * find findProdExcipientE
	 * @param amd
	 * @param isProdNotNull if true - find getProduct() != null
	 * @return
	 */
	public ProdExcipient findProdExcipientEdit(AmdREPLACEMENT_IPI amd, boolean isProdNotNull){
		List<ProdExcipient> list = amd.getProdExcipient();
		if(list != null && list.size() == 2){
			if(isProdNotNull){
				if(list.get(0).getProduct() != null)
					return list.get(0);
				if(list.get(1).getProduct() != null)
					return list.get(1);	
			}else{
				if(list.get(0).getProduct() == null)
					return list.get(0);
				if(list.get(1).getProduct() == null)
					return list.get(1);	
			}
		}
		return null;
	}

	public void initialize(Amendment amd) {
		Hibernate.initialize(amd.getProduct());
		Hibernate.initialize(amd.getApplicant());
		Hibernate.initialize(amd.getDictionary());

		Hibernate.initialize(amd.getDocuments());
		if(amd.getDocuments()!=null){
			for(AmdDocuments d: amd.getDocuments()){
				Hibernate.initialize(d);
				if(d!=null)
					Hibernate.initialize(d.getDocument());				
			}
		}

		Hibernate.initialize(amd.getConditions());
		if(amd.getConditions()!=null){
			for(AmdConditions c : amd.getConditions()){
				Hibernate.initialize(c);
				if(c!=null)
					Hibernate.initialize(c.getCondition());
			}
		}

		Hibernate.initialize(amd.getProdAppChecklist());
		if(amd.getProdAppChecklist()!=null){
			for(ProdAppChecklist c : amd.getProdAppChecklist()){
				Hibernate.initialize(c);
				if(c != null)
					Hibernate.initialize(c.getChecklist());
			}
		}
	}

	/**
	 * find Amendment  by ProdApplications id
	 * @param prodAppID
	 * @return
	 */
	public Amendment findAmendmentByProdApp(Long prodAppID){
		Amendment amd = null;
		String hql = "SELECT amd FROM Amendment amd "
				+ " left join amd.prodApplications as prodapp "
				+ " WHERE amd.state=:st"
				+ " and prodapp.id = " + prodAppID;
		TypedQuery<Amendment> query = getEntityManager().createQuery(hql, Amendment.class);
		List<Amendment> list = query.setParameter("st", AmdState.SUBMITTED).getResultList();
		if(list != null && list.size() > 0)
			amd = list.get(0);
		return amd;
	}

	private ProdInn findProdInn(Long prodId, Long innId, Long compId){
		if(innId != null && innId > 0
				&& compId != null && compId > 0){
			List<ProdInn> list = prodInnDAO.findByProduct_Id(prodId);
			if(list != null && list.size() > 0){
				for(ProdInn pr:list){
					Long inn_id = 0L;
					Long comp_id = 0L;
					if(pr.getInn() != null)
						inn_id = pr.getInn().getId();
					if(pr.getCompany() != null)
						comp_id = pr.getCompany().getId();

					if(inn_id.intValue() == innId.intValue() &&
							comp_id.intValue() == compId.intValue())
						return pr;
				}
			}
		}
		return null;
	}

	private ProdCompany findProdCompany(Long prodId, Long compId, CompanyType type){
		if(compId != null && compId > 0 && type != null){
			List<ProdCompany> list = prodCompanyDAO.findByProduct_Id(prodId);
			if(list != null && list.size() > 0){
				for(ProdCompany pc:list){
					CompanyType t = pc.getCompanyType();
					Long comp_id = 0L;
					if(pc.getCompany() != null)
						comp_id = pc.getCompany().getId();

					if(comp_id.intValue() == compId.intValue()
							&& type.name().equals(t.name()))
						return pc;
				}
			}
		}
		return null;
	}

	public Amendment saveAndFlush(Amendment amd){
		return amendmentDAO.saveAndFlush(amd);
	}

	public AmdREMOVE_MANUF saveAndFlush(AmdREMOVE_MANUF amd){
		amd = amdREMOVE_MANUFDAO.saveAndFlush(amd);
		return amd;
	}

	public AmdADD_MANUF saveAndFlush(AmdADD_MANUF amd) {
		if(amd.getState() != null && 
				(amd.getState().equals(AmdState.RECOMMENDED) ||
						amd.getState().equals(AmdState.NOT_RECOMMENDED) ||
						amd.getState().equals(AmdState.REJECTED) ||
						amd.getState().equals(AmdState.APPROVED))){
			amd = amdADD_MANUFDAO.saveAndFlush(amd);
		}else{
			List<ProdCompany> list = amd.getProdCompany();
			List<ProdCompany> newList = null;
			if(list != null && list.size() > 0){
				newList = new ArrayList<ProdCompany>();
				for(ProdCompany p:list){
					if(p.getId() != null){
						p = prodCompanyDAO.findOne(p.getId());
					}else{
						p = prodCompanyDAO.saveAndFlush(p);
					}
					newList.add(p);
				}
			}
			amd.setProdCompany(newList);
			amd = amdADD_MANUFDAO.saveAndFlush(amd);
		}
		return amd;
	}


	public AmdREPLACEMENT_IPI saveAndFlush(AmdREPLACEMENT_IPI amd){
		if(amd.getState() != null && 
				(amd.getState().equals(AmdState.RECOMMENDED) ||
						amd.getState().equals(AmdState.NOT_RECOMMENDED) ||
						amd.getState().equals(AmdState.REJECTED) ||
						amd.getState().equals(AmdState.APPROVED))){
			amd = amdREPLACEMENT_IPIDAO.saveAndFlush(amd);
			// remove old or empty data
			removeEmptyLinkProdExcipient();
		}else{
			List<ProdExcipient> list = amd.getProdExcipient();
			List<ProdExcipient> newList = null;
			if(list != null && list.size() == 2){
				newList = new ArrayList<ProdExcipient>();
				// index = 0 - value edit
				// index = 1 - new or updated value - always save
				ProdExcipient p = prodExcipientDAO.findOne(list.get(0).getId());
				newList.add(p);
				list.get(1).setExcipient(excipientDAO.findExcipientById(list.get(1).getExcipient().getId()));
				p = prodExcipientDAO.saveAndFlush(list.get(1));
				newList.add(p);
			}
			amd.setProdExcipient(newList);
			amd = amdREPLACEMENT_IPIDAO.saveAndFlush(amd);
		}
		return amd;
	}

	public AmdEDIT_API saveAndFlush(AmdEDIT_API amd) {
		if(amd.getState() != null && 
				(amd.getState().equals(AmdState.RECOMMENDED) ||
						amd.getState().equals(AmdState.NOT_RECOMMENDED) ||
						amd.getState().equals(AmdState.REJECTED) ||
						amd.getState().equals(AmdState.APPROVED))){
			amd = amdEDIT_APIDAO.saveAndFlush(amd);
			// remove old or empty data
			//removeEmptyLinkProdInn();
		}else{
			List<ProdInn> list = amd.getProdInn();
			List<ProdInn> newList = null;
			if(list != null && list.size() == 2){
				newList = new ArrayList<ProdInn>();
				// index = 0 - value edit
				// index = 1 - new or updated value - always save
				ProdInn p = prodInnDAO.findOne(list.get(0).getId());
				newList.add(p);

				p = prodInnDAO.saveAndFlush(list.get(1));
				newList.add(p);
			}
			amd.setProdInn(newList);
			amd = amdEDIT_APIDAO.saveAndFlush(amd);
		}
		return amd;
	}

	public AmdPRODUCT_NAME saveAndFlush(AmdPRODUCT_NAME amd){
		amd = amdPRODUCT_NAMEDAO.saveAndFlush(amd);
		initialize(amd);
		return amd;
	}

	public AmdAPPLICANT saveAndFlush(AmdAPPLICANT amd) {
		amd = amdAPPLICANTDAO.saveAndFlush(amd);
		initialize(amd);
		return amd;
	}

	public AmdREMOVE_IPI saveAndFlush(AmdREMOVE_IPI amd) {
		amd = amdREMOVE_IPIDAO.saveAndFlush(amd);
		return amd;
	}

	public AmdADD_IPI saveAndFlush(AmdADD_IPI amd) {
		if(amd.getState() != null && 
				(amd.getState().equals(AmdState.RECOMMENDED) ||
						amd.getState().equals(AmdState.NOT_RECOMMENDED) ||
						amd.getState().equals(AmdState.REJECTED) ||
						amd.getState().equals(AmdState.APPROVED))){
			amd = amdADD_IPIDAO.saveAndFlush(amd);
		}else{
			List<ProdExcipient> list = amd.getProdExcipient();
			List<ProdExcipient> newList = null;
			if(list != null && list.size() > 0){
				newList = new ArrayList<ProdExcipient>();
				for(ProdExcipient p:list){
					if(p.getId() != null){
						p = prodExcipientDAO.findOne(p.getId());
					}else{
						p = prodExcipientDAO.saveAndFlush(p);
					}
					newList.add(p);
				}
			}
			amd.setProdExcipient(newList);
			amd = amdADD_IPIDAO.saveAndFlush(amd);
		}
		return amd;
	}

	public AmdSHAPE saveAndFlush(AmdSHAPE amd) {
		amd = amdSHAPEDAO.saveAndFlush((AmdSHAPE)amd);
		initialize(amd);
		return amd;
	}	

	public AmdPRODUCT_DOSFORM saveAndFlush(AmdPRODUCT_DOSFORM amd){
		DosageForm df = null;
		if(amd.getDosageForm() != null){
			df = dosageFormDAO.findOne(amd.getDosageForm().getUid());
		}
		amd.setDosageForm(df);
		amd = amdPRODUCT_DOSFORMDAO.saveAndFlush(amd);
		initialize(amd);
		Hibernate.initialize(amd.getDosageForm());
		return amd;
	}

	public AmdPRODUCT_ADMINROUTE saveAndFlush(AmdPRODUCT_ADMINROUTE amd){
		if(amd.getAdminRoute() != null){
			AdminRoute ar = adminRouteDAO.findOne(amd.getAdminRoute().getId());
			amd.setAdminRoute(ar);
		}
		amd = amdPRODUCT_ADMINROUTEDAO.saveAndFlush(amd);
		initialize(amd);
		Hibernate.initialize(amd.getAdminRoute());
		return amd;
	}

	public AmdSHELF_LIFE saveAndFlush(AmdSHELF_LIFE amd){
		amd = amdSHELF_LIFEDAO.saveAndFlush(amd);
		initialize(amd);
		return amd;
	}

	public AmdADD_API saveAndFlush(AmdADD_API amd) {
		if(amd.getState() != null && 
				(amd.getState().equals(AmdState.RECOMMENDED) ||
						amd.getState().equals(AmdState.NOT_RECOMMENDED) ||
						amd.getState().equals(AmdState.REJECTED) ||
						amd.getState().equals(AmdState.APPROVED))){
			amd = amdADD_APIDAO.saveAndFlush(amd);
		}else{
			List<ProdInn> list = amd.getProdInn();
			List<ProdInn> newList = null;
			if(list != null && list.size() > 0){
				newList = new ArrayList<ProdInn>();
				for(ProdInn p:list){
					if(p.getId() != null){
						p = prodInnDAO.findOne(p.getId());
					}else{
						p = prodInnDAO.saveAndFlush(p);
					}
					newList.add(p);
				}
			}
			amd.setProdInn(newList);
			amd = amdADD_APIDAO.saveAndFlush(amd);
		}
		return amd;
	}

	public AmdREMOVE_API saveAndFlush(AmdREMOVE_API amd) {
		amd = amdREMOVE_APIDAO.saveAndFlush(amd);
		return amd;
	}

	/**
	 * implementation Amendment AmdAPPLICANT
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdAPPLICANT(AmdAPPLICANT amendment, String subjectAmd, User curUser) {
		// save
		AmdAPPLICANT amd = amdAPPLICANTDAO.saveAndFlush(amendment);

		// change 
		Applicant appl = amd.getApplicant();
		String str = appl.getAppName();
		appl.setAppName(amd.getApplName());
		amd.setApplName(str);

		str = appl.getAddress().getAddress1();
		appl.getAddress().setAddress1(amd.getAddress().getAddress1());
		amd.getAddress().setAddress1(str);

		str = appl.getAddress().getAddress2();
		appl.getAddress().setAddress2(amd.getAddress().getAddress2());
		amd.getAddress().setAddress2(str);

		str = appl.getAddress().getZipcode();
		appl.getAddress().setZipcode(amd.getAddress().getZipcode());
		amd.getAddress().setZipcode(str);

		Country cntr = appl.getAddress().getCountry();
		appl.getAddress().setCountry(amd.getAddress().getCountry());
		amd.getAddress().setCountry(cntr);

		// save Applicant
		applicantDAO.saveApplicant(appl);

		amd = amdAPPLICANTDAO.saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdPRODUCT_NAME
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdPRODUCT_NAME(AmdPRODUCT_NAME amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdPRODUCT_NAME amd = saveAndFlush(amendment);
		// change 		
		Product product = amd.getProduct();
		String str = product.getProdName();
		product.setProdName(amd.getPropName());
		amd.setPropName(str);				
		// save Product
		productDAO.saveProduct(product);

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdADD_MANUF
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdADD_MANUF(AmdADD_MANUF amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdADD_MANUF amd = saveAndFlush(amendment);
		amd = findAmdADD_MANUF(amd.getId());

		Product prod = null;
		if(prodApp != null)
			prod = productDAO.findProduct(prodApp.getProduct().getId());
		else
			prod = productDAO.findProduct(amendment.getProduct().getId());

		List<ProdCompany> list = amendment.getProdCompany();
		if(list != null && list.size() > 0){
			for(ProdCompany p:list){
				p.setProduct(prod);
				prodCompanyDAO.saveAndFlush(p);

				if(p.getCompanyType() != null && p.getCompanyType().equals(CompanyType.FIN_PROD_MANUF)){
					prod.setManufName(p.getCompany().getCompanyName());
					productDAO.saveProduct(prod);
				}
			}
		}

		amendment = saveAndFlush(amendment);
		if(amendment != null)
			return createMemo(amendment, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdREPLACEMENT_IPI
	 * and send memo
	 * 
	 * get 2 prodExcipient
	 * 1) set link to Product where product is null
	 * 2) set null where product is NOT null
	 * @param amendment
	 * @return
	 */
	public String implementAmdREPLACEMENT_IPI(AmdREPLACEMENT_IPI amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		AmdREPLACEMENT_IPI amd = saveAndFlush(amendment);
		amd = findAmdREPLACEMENT_IPI(amd.getId());

		List<ProdExcipient> list = amd.getProdExcipient();
		if(list != null && list.size() == 2){
			ProdExcipient oldValue = null;
			ProdExcipient newValue = null;
			if(list.get(0).getProduct() != null && list.get(1).getProduct() == null){
				oldValue = list.get(0);
				newValue = list.get(1);
			}
			if(list.get(0).getProduct() == null && list.get(1).getProduct() != null){
				oldValue = list.get(1);
				newValue = list.get(0);
			}
			if(oldValue != null && newValue != null){
				newValue.setProduct(oldValue.getProduct());
				prodExcipientDAO.saveAndFlush(newValue);

				oldValue.setProduct(null);
				prodExcipientDAO.saveAndFlush(oldValue);
			}
		}

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdADD_IPI
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdADD_IPI(AmdADD_IPI amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdADD_IPI amd = saveAndFlush(amendment);
		amd = findAmdADD_IPI(amd.getId());
		Product prod = null;
		if(prodApp != null)
			prod = prodApp.getProduct();
		else
			prod = amd.getProduct();

		List<ProdExcipient> list = amendment.getProdExcipient();
		if(list != null && list.size() > 0){
			for(ProdExcipient pexc:list){
				pexc.setProduct(prod);
				prodExcipientDAO.saveAndFlush(pexc);
			}
		}
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdREMOVE_IPI
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdREMOVE_IPI(AmdREMOVE_IPI amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdREMOVE_IPI amd = saveAndFlush(amendment);
		amd = findAmdREMOVE_IPI(amendment.getId());

		List<ProdExcipient> list = amendment.getProdExcipient();
		if(list != null && list.size() > 0){
			for(ProdExcipient pexc:list){
				pexc.setProduct(null);
				prodExcipientDAO.saveAndFlush(pexc);
			}
		}
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdREMOVE_API
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdREMOVE_API(AmdREMOVE_API amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdREMOVE_API amd = saveAndFlush(amendment);
		amd = findAmdREMOVE_API(amendment.getId());

		List<ProdInn> list = amendment.getProdInn();
		if(list != null && list.size() > 0){
			for(ProdInn pexc:list){
				pexc.setProduct(null);
				prodInnDAO.saveAndFlush(pexc);
			}
		}
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdEDIT_API
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdEDIT_API(AmdEDIT_API amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		AmdEDIT_API amd = saveAndFlush(amendment);
		amd = findAmdEDIT_API(amd.getId());

		List<ProdInn> list = amd.getProdInn();
		if(list != null && list.size() == 2){
			ProdInn oldValue = null;
			ProdInn newValue = null;
			if(list.get(0).getProduct() != null && list.get(1).getProduct() == null){
				oldValue = list.get(0);
				newValue = list.get(1);
			}
			if(list.get(0).getProduct() == null && list.get(1).getProduct() != null){
				oldValue = list.get(1);
				newValue = list.get(0);
			}
			if(oldValue != null && newValue != null){
				newValue.setProduct(oldValue.getProduct());
				prodInnDAO.saveAndFlush(newValue);

				oldValue.setProduct(null);
				prodInnDAO.saveAndFlush(oldValue);
			}
		}

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdREMOVE_MANUF
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdREMOVE_MANUF(AmdREMOVE_MANUF amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdREMOVE_MANUF amd = saveAndFlush(amendment);
		amd = findAmdREMOVE_MANUF(amendment.getId());

		List<ProdCompany> list = amendment.getProdCompany();
		if(list != null && list.size() > 0){
			for(ProdCompany it:list){
				it.setProduct(null);
				prodCompanyDAO.saveAndFlush(it);
			}
		}

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdSHAPE
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdSHAPE(AmdSHAPE amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdSHAPE amd = saveAndFlush(amendment);
		// change 
		Product prod = null;
		if(prodApp != null)
			prod = prodApp.getProduct();
		else
			prod = amd.getProduct();

		String str = prod.getProdDesc();
		prod.setProdDesc(amd.getProdDesc());
		amd.setProdDesc(str);				
		// save Product
		productDAO.saveProduct(prod);

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdSHELF_LIFE
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdSHELF_LIFE(AmdSHELF_LIFE amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdSHELF_LIFE amd = saveAndFlush(amendment);
		// change 		
		Product prod = null;
		if(prodApp != null)
			prod = prodApp.getProduct();
		else
			prod = amd.getProduct();
		String str = prod.getShelfLife();
		prod.setShelfLife(amd.getShelfLife());
		amd.setShelfLife(str);				
		// save Product
		productDAO.saveProduct(prod);

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdPRODUCT_DOSFORM
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdPRODUCT_DOSFORM(AmdPRODUCT_DOSFORM amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdPRODUCT_DOSFORM amd = saveAndFlush(amendment);

		DosageForm newDosForm = amd.getDosageForm();
		Product prod = null;
		if(prodApp != null)
			prod = prodApp.getProduct();
		else
			prod = amd.getProduct();

		DosageForm old = prod.getDosForm();
		amd.setDosageForm(old);
		prod.setDosForm(newDosForm);
		amd.setProduct(prod);
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdPRODUCT_ADMINROUTE
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdPRODUCT_ADMINROUTE(AmdPRODUCT_ADMINROUTE amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdPRODUCT_ADMINROUTE amd = saveAndFlush(amendment);

		AdminRoute newValue = amd.getAdminRoute();
		Product prod = null;
		if(prodApp != null){
			prod = prodApp.getProduct();

			AdminRoute oldValue = prod.getAdminRoute();
			amd.setAdminRoute(oldValue);
			prod.setAdminRoute(newValue);
			amd.setProduct(prod);
			amd = saveAndFlush(amd);
			if(amd != null)
				return createMemo(amd, subjectAmd, false, curUser);
		}

		return "global_fail";
	}

	/**
	 * implementation Amendment AmdADD_API
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmdADD_API(AmdADD_API amendment, ProdApplications prodApp, String subjectAmd, User curUser) {
		// save
		AmdADD_API amd = saveAndFlush(amendment);
		amd = findAmdADD_API(amd.getId());
		Product prod = null;
		if(prodApp != null)
			prod = prodApp.getProduct();
		else
			prod = amd.getProduct();

		List<ProdInn> list = amendment.getProdInn();
		if(list != null && list.size() > 0){
			for(ProdInn p:list){
				p.setProduct(prod);
				prodInnDAO.saveAndFlush(p);
			}
		}
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * implementation Amendment
	 * and send memo
	 * @param amendment
	 * @return
	 */
	public String implementAmendment(Amendment amendment, String subjectAmd, User curUser) {
		Amendment amd = saveAndFlush(amendment);
		if(amd != null)
			return createMemo(amd, subjectAmd, false, curUser);

		return "global_fail";
	}

	/**
	 * validate add Inn
	 * uniqe key inn&&manufacturer
	 * @param ProdInn
	 * @param amd
	 * @return
	 */
	public String validateListProdInn(List<ProdInn> newList, Amendment amd){
		String error = "";
		List<ProdInn> allList = new ArrayList<ProdInn>();
		List<ProdInn> saveList = fetchProdInns(amd);
		if(saveList != null && saveList.size() > 0)
			allList.addAll(saveList);
		if(newList != null && newList.size() > 0)
			allList.addAll(newList);

		error = productService.varificationInnProduct(allList);

		return error;
	}

	/**
	 * varification select Applicant on tab Applicant
	 * @param amd
	 * @return
	 */
	public String validateSelectApplicant(Amendment amd){
		if(amd.getApplicant() != null && 
				amd.getApplicant().getApplcntId() > 0 &&
				amd.getApplicant().getAppName().length() > 0){
			return "";
		}else{
			return "selectapplicant";
		}
	}

	/**
	 * varification select Product on tab Product
	 * @param amd
	 * @return
	 */
	public String validateSelectProduct(Amendment amd){
		if(amd.getProduct() != null && 
				amd.getProduct().getId() > 0){
			return "";
		}else{
			return "selectapplication";
		}
	}

	public Long createNewApplication(Amendment amd, User curUser){
		Applicant applicant = amd.getApplicant();
		Product prod = amd.getProduct();
		ProdAppType type = productService.getProdAppTypeByProduct(prod.getId());
		User resp = userDAO.findByUsername(applicant.getContactName());
		FeePayment payment = amd.getPayment();

		ProdApplications prodApp = new ProdApplications(prod, applicant);
		prodApp.setActive(true);
		prodApp.setCreatedDate(amd.getCreatedDate());
		prodApp.setUpdatedDate(new Date());
		prodApp.setProdAppType(type);
		prodApp.setRegState(RegState.NEW_APPL);
		prodApp.setSubmitDate(new Date());
		prodApp.setApplicantUser(resp);
		prodApp.setPaymentPrescreen(payment);
		prodApp.setCreatedBy(curUser);
		prodApp.setUpdatedBy(curUser);

		prodApp = prodApplicationsService.saveApplication(prodApp, curUser.getUserId());
		List<ProdAppChecklist> checklist = amd.getProdAppChecklist();
		if(checklist != null){
			for(ProdAppChecklist it:checklist){
				it.setProdApplications(prodApp);
			}
		}
		prodApplicationsService.saveProdAppChecklists(checklist);

		//20/09/17
		//TODO
		/*--
		 prodApp.setProdAppNo(getProdApplicationsService().generateAppNo(prodApp));
		 prodApp.setProdSrcNo(getProdApplicationsService().getSrcNumber(prodApp.getProdAppNo()));
		 */


		prodApp = prodApplicationsService.saveApplication(prodApp, curUser.getUserId());
		prodApp.setAmendment(amd);
		amd.setProdApplications(prodApp);
		saveAndFlush(amd);

		if(prodApp != null)
			return prodApp.getId();

		return null;
	}

	private String createMemo(Amendment amd, String subjectAmd, boolean isReject, User curUser){
		String err = "";
		//
		if(amd.getCreatedBy() != null && amd.getCreatedBy().isCompany()){
			String email = amd.getCreatedBy().getEmail();
			if(email != null && email.length() > 0){
				Mail mail = new Mail();
				mail.setMailto(email);
				mail.setSubject("AMENDMENT TO PRODUCT");
				mail.setUser(curUser);
				mail.setDate(new Date());

				String result = "approved";
				if(isReject)
					result = "rejected";

				String text = "Dear Sir/Madam, <br /><br /> " + subjectAmd + "<br /><br />";
				if(amd.getProduct() != null)
					text += "Your amendment for product " + amd.getProduct().getProdName();
				else
					text += "Your amendment ";
				text += " has been " + result + ". Please check Product Record. <br />" + 
						"This letter is created by robot. Please do not reply.";

				mail.setMessage(text);
				String sender = workspaceDAO.findAll().get(0).getRegistraremail();
				if (sender==null)
					sender = "info@msh.org";
				else if ("".equals(sender))
					sender = "info@msh.org";
				try {
					mailService.sendMailWithHTML(mail, false, sender);
				} catch (MessagingException e) {
					e.printStackTrace();
					err = "error_sendEmail";
				}
			}else
				err = "error_sendEmail";
		}

		return err;
	}
	/**
	 * reject AmdAPPLICANT and send memo
	 * @return
	 */
	public String rejectAmdAPPLICANT(AmdAPPLICANT amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdPRODUCT_NAME and send memo
	 * @return
	 */
	public String rejectAmdPRODUCT_NAME(AmdPRODUCT_NAME amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdADD_MANUF and send memo
	 * @return
	 */
	public String rejectAmdADD_MANUF(AmdADD_MANUF amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdADD_IPI and send memo
	 * @return
	 */
	public String rejectAmdADD_IPI(AmdADD_IPI amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		//TODO
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdADD_API and send memo
	 * @return
	 */
	public String rejectAmdADD_API(AmdADD_API amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		//TODO
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdEDIT_API and send memo
	 * @return
	 */
	public String rejectAmdEDIT_API(AmdEDIT_API amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		//TODO
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdREMOVE_API and send memo
	 * @return
	 */
	public String rejectAmdREMOVE_API(AmdREMOVE_API amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		//TODO
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdREMOVE_IPI and send memo
	 * @return
	 */
	public String rejectAmdREMOVE_IPI(AmdREMOVE_IPI amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		//TODO
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdREMOVE_MANUF and send memo
	 * @return
	 */
	public String rejectAmdREMOVE_MANUF(AmdREMOVE_MANUF amd, String subjectAmd, User curUser){
		//amd = saveAndFlush(amd);
		//Hibernate.initialize(amd);
		//Hibernate.initialize(amd.getProdInn());
		//Hibernate.initialize(amd.getProdCompany());
		// change 
		/*List<ProdInn> inns = amd.getProdInn();
		if(inns != null && inns.size() > 0){
			for(int i = 0; i < inns.size(); i++){
				ProdInn inn = inns.get(i);
				inn.setAmendment(null);
				prodInnDAO.saveAndFlush(inn);
			}
		}*/

		/*List<ProdCompany> comps = amd.getProdCompany();
		if(comps != null && comps.size() > 0){
			for(int i = 0; i < comps.size(); i++){
				ProdCompany pc = comps.get(i);
				pc.setAmendment(null);
				prodCompanyDAO.saveAndFlush(pc);
			}
		}*/

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdREPLACEMENT_IPI and send memo
	 * @return
	 */
	public String rejectAmdREPLACEMENT_IPI(AmdREPLACEMENT_IPI amd, String subjectAmd, User curUser){
		/*amd = saveAndFlush(amd);
		Hibernate.initialize(amd);
		Hibernate.initialize(amd.getProdInn());
		Hibernate.initialize(amd.getProdCompany());
		// change 
		List<ProdInn> inns = amd.getProdInn();
		if(inns != null && inns.size() > 0){
			for(int i = 0; i < inns.size(); i++){
				ProdInn inn = inns.get(i);
				inn.setAmendment(null);
				getProdInnDAO().saveAndFlush(inn);
			}
		}

		List<ProdCompany> comps = amd.getProdCompany();
		if(comps != null && comps.size() > 0){
			for(int i = 0; i < comps.size(); i++){
				ProdCompany pc = comps.get(i);
				pc.setAmendment(null);
				getProdCompanyDAO().saveAndFlush(pc);
			}
		}*/

		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdSHELF_LIFE and send memo
	 * @return
	 */
	public String rejectAmdSHELF_LIFE(AmdSHELF_LIFE amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdSHAPE and send memo
	 * @return
	 */
	public String rejectAmdSHAPE(AmdSHAPE amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdPRODUCT_DOSFORM and send memo
	 * @return
	 */
	public String rejectAmdPRODUCT_DOSFORM(AmdPRODUCT_DOSFORM amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject AmdPRODUCT_ADMINROUTE and send memo
	 * @return
	 */
	public String rejectAmdPRODUCT_ADMINROUTE(AmdPRODUCT_ADMINROUTE amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}

	/**
	 * reject Amendment and send memo
	 * @return
	 */
	public String rejectAmendment(Amendment amd, String subjectAmd, User curUser){
		amd = saveAndFlush(amd);
		if(amd != null)
			return createMemo(amd, subjectAmd, true, curUser);
		return "global_fail";
	}
	public ProdExcipientDAO getProdExcipientDAO() {
		return prodExcipientDAO;
	}
	public void setProdExcipientDAO(ProdExcipientDAO prodExcipientDAO) {
		this.prodExcipientDAO = prodExcipientDAO;
	}
	public ProdInnDAO getProdInnDAO() {
		return prodInnDAO;
	}
	public void setProdInnDAO(ProdInnDAO prodInnDAO) {
		this.prodInnDAO = prodInnDAO;
	}
	public CompanyDAO getCompanyDAO() {
		return companyDAO;
	}
	public void setCompanyDAO(CompanyDAO companyDAO) {
		this.companyDAO = companyDAO;
	}


}
