package org.msh.pharmadex.service.amendment;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hibernate.Hibernate;
import org.msh.pharmadex.dao.iface.AmendmentDictionaryDAO;
import org.msh.pharmadex.domain.amendment.AmdChecklist;
import org.msh.pharmadex.domain.amendment.AmdConditions;
import org.msh.pharmadex.domain.amendment.AmdDocuments;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.amendment.AmendmentCondition;
import org.msh.pharmadex.domain.amendment.AmendmentDictionary;
import org.msh.pharmadex.domain.amendment.AmendmentDocument;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.AmendmentSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AmendmentDictionaryService {

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	private AmendmentService amendmentService;

	@Autowired
	AmendmentDictionaryDAO amendmentDictionaryDAO;

	public EntityManager getEntityManager() {
		return entityManager;
	}
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public AmendmentDictionaryDAO getAmendmentDictionaryDAO() {
		return amendmentDictionaryDAO;
	}
	public void setAmendmentDictionaryDAO(AmendmentDictionaryDAO amendmentDictionaryDAO) {
		this.amendmentDictionaryDAO = amendmentDictionaryDAO;
	}

	public AmendmentDictionary findAmendmentDictionary(Long id){
		AmendmentDictionary it = getAmendmentDictionaryDAO().findOne(id);
		Hibernate.initialize(it.getAmendmentConditions());
		Hibernate.initialize(it.getAmendmentDocuments());

		return it;
	}

	public List<AmendmentDictionary> findAll() {
		List<AmendmentDictionary> list = null;
		String hql = "SELECT d FROM AmendmentDictionary d "
				+ " order by d.chapter";
		TypedQuery<AmendmentDictionary> query = getEntityManager().createQuery(hql, AmendmentDictionary.class);
		list = query.getResultList();

		return list;
	}

	/**
	 * 
	 * @param amdSubj - choose Subject from SelectedOne
	 * @param amdProc - choose Procces from SelectedOne
	 * @param isEdit - true - getAll, false - only active=true
	 * @return
	 */
	public List<AmendmentDictionary> findAll(AmendmentSubject amdSubj, AmendmentProcess amdProc, boolean isEdit) {
		List<AmendmentDictionary> list = null;
		String hql = "SELECT d FROM AmendmentDictionary d ";
		//+ " order by d.chapter";
		String addHql = "";
		if(amdSubj != null)
			addHql = " d.subject like '" + amdSubj.name() + "'";
		if(amdProc != null){
			if(addHql.length() > 0)
				addHql += " and";

			addHql += " d.process like '" + amdProc.name() + "'";
		}
		if(!isEdit){
			if(addHql.length() > 0)
				addHql += " and ";
			addHql += " d.active=1 ";
		}

		hql = hql + (addHql.length() > 0 ? (" where " + addHql) : "") + " order by d.id desc";
		TypedQuery<AmendmentDictionary> query = getEntityManager().createQuery(hql, AmendmentDictionary.class);
		list = query.getResultList();

		return list;
	}

	public List<AmendmentCondition> fetchAmendmentConditionByDictionary(Long amdDictID){
		if(amdDictID != null && amdDictID > 0){
			AmendmentDictionary dictionary = getAmendmentDictionaryDAO().findOne(amdDictID);
			if(dictionary != null)
				return dictionary.getAmendmentConditions();
		}
		return null;
	}

	public List<AmdConditions> fetchAmdConditionByDictionary(Long amdDictID){
		List<AmdConditions> result = null;
		List<AmendmentCondition> list = fetchAmendmentConditionByDictionary(amdDictID);
		if(list != null && list.size() > 0){
			result = new ArrayList<AmdConditions>();
			for(AmendmentCondition cond:list){
				if(cond.isActive()){
					AmdConditions amd = new AmdConditions();
					amd.setCondition(cond);
					amd.setAnswer(new AmdChecklist());

					result.add(amd);
				}
			}
		}
		return result;
	}

	public List<AmendmentDocument> fetchAmendmentDocumentByDictionary(Long amdDictID){
		AmendmentDictionary dictionary = getAmendmentDictionaryDAO().findOne(amdDictID);
		if(dictionary != null)
			return dictionary.getAmendmentDocuments();

		return null;
	}

	/**
	 * getAll AmendmentDocument by current Dictionary
	 * if amdId > 0 then AmdDocuments set value
	 * @param amdDictID
	 * @param amdId
	 * @return
	 */
	public List<AmdDocuments> fetchAmdDocumentByDictionary(AmendmentDictionary amdDict, Amendment amendment){
		List<AmdDocuments> result = null;
		if(amdDict != null){
			List<AmendmentDocument> list = fetchAmendmentDocumentByDictionary(amdDict.getId());
			if(list != null && list.size() > 0){
				result = new ArrayList<AmdDocuments>();
				for(AmendmentDocument d:list){
					if(d.isActive()){
						if(!isListContainValue(result, d)){
							AmdDocuments amd = new AmdDocuments();
							amd.setDocument(d);
							amd.setAnswer(new AmdChecklist());
							if(amendment != null)
								amd.setAmendment(amendment);
							result.add(amd);
						}
					}
				}
			}
		}
		Hibernate.initialize(result);
		return result;
	}

	private boolean isListContainValue(List<AmdDocuments> list, AmendmentDocument doc){
		if(list != null && list.size() > 0 && doc != null){
			for(AmdDocuments amdDoc:list){
				if(amdDoc.getDocument() != null &&
						amdDoc.getDocument().getId().intValue() == doc.getId().intValue())
					return true && (amdDoc.getAnswer() != null);
			}
		}

		return false;
	}

	public List<String> fetchListChapters(){
		List<String> chapters = new ArrayList<String>();
		String hql = "SELECT distinct d.chapter FROM AmendmentDictionary as d";
		TypedQuery<String> query = getEntityManager().createQuery(hql, String.class);
		chapters = query.getResultList();
		return chapters;
	}

	public List<String> fetchListSubChapters(String chapter){
		List<String> subChapters = new ArrayList<String>();
		String hql = "SELECT distinct d.subChapter FROM AmendmentDictionary as d where d.chapter like '" + chapter + "'";
		TypedQuery<String> query = getEntityManager().createQuery(hql, String.class);
		subChapters = query.getResultList();
		return subChapters;
	}

	/**
	 * chapter&&subchapter - unique
	 * @param dictionary
	 * @return
	 */
	public String varificationAmendmentDictionary(AmendmentDictionary dictionary){
		String error = "";
		if(dictionary.getFormulation() != null && dictionary.getFormulation().length() > 1024)
			error = "error_maxLength";
		//TODO complete body
		/*		String hql = "SELECT d FROM AmendmentDictionary as d"
				+ " where d.chapter like '" + dictionary.getChapter() + "'"
				+ " and d.subChapter like '" + dictionary.getSubChapter() + "'";
		if(dictionary.getId() != null && dictionary.getId() > 0)
			hql += " and d.id != " + dictionary.getId();
		TypedQuery<AmendmentDictionary> query = getEntityManager().createQuery(hql, AmendmentDictionary.class);
		List<AmendmentDictionary> list = query.getResultList();
		if(list != null && list.size() > 0)
			error = "error_dublicate_amdDictionary";*/
		return error;
	}

	public String varificationAmendmentCondition(AmendmentCondition item){
		String error = "";
		if(item.getCondition() != null && item.getCondition().length() > 1024)
			error = "error_maxLength";
		return error;
	}

	public String varificationAmendmentDocument(AmendmentDocument item){
		String error = "";
		if(item.getName() != null && item.getName().length() > 1024)
			error = "error_maxLength";
		return error;
	}
	public AmendmentService getAmendmentService() {
		return amendmentService;
	}
	public void setAmendmentService(AmendmentService service) {
		this.amendmentService = service;
	}
}
