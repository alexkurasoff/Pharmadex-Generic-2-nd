package org.msh.pharmadex.dao;

import org.msh.pharmadex.domain.ReviewDetail;
import org.msh.pharmadex.domain.ReviewQuestion;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Repository
@Transactional
public class ReviewQDAO implements Serializable{

    @PersistenceContext
    EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ReviewQuestion> findAll(){
        return entityManager.createQuery(" select rq from ReviewQuestion rq").getResultList();
    }

    public List<ReviewQuestion> findBySRA() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.sra = true")
                .getResultList();
    }

    public List<ReviewQuestion> findByGenMed() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.genMed = true")
                .getResultList();
    }

    public List<ReviewQuestion> findByRenewal() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.renewal = true")
                .getResultList();
    }

    public List<ReviewQuestion> findByNewMolecule() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.newMed = true")
                .getResultList();
    }
    
    public List<ReviewQuestion> findByVariation() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.variation = true")
                .getResultList();
    }
    
    public List<ReviewQuestion> findByMajVariation() {
        return entityManager.createQuery(" select rq from ReviewQuestion rq where rq.majVariation = true")
                .getResultList();
    }

    @Transactional(readOnly = true)
    public ReviewQuestion findOne(Long id){
        return entityManager.find(ReviewQuestion.class, id);
    }

    @Transactional
    public List<ReviewDetail> findReviewSummary(Long userID, Long reviewInfoID){
        return entityManager.createQuery("select rd from ReviewDetail rd where rd.reviewInfo.reviewer.userId = :userID and rd.reviewInfo.id = :reviewInfoID")
                .setParameter("userID", userID)
                .setParameter("reviewInfoID", reviewInfoID)
                .getResultList();

    }
    
    @Transactional
    public List<ReviewDetail> findReviewSummary2Module(Long userID, Long reviewInfoID){
        return entityManager.createQuery("select rd from ReviewDetail rd where rd.reviewInfo.reviewer.userId = :userID and rd.reviewInfo.id = :reviewInfoID and rd.reviewQuestions.ctdModule like 'MODULE_3'")
                .setParameter("userID", userID)
                .setParameter("reviewInfoID", reviewInfoID)
                .getResultList();

    }
   
   
    @Transactional
	public boolean save(ReviewQuestion listItem) {
	if (listItem.getId()==null) entityManager.persist(listItem);
	else 
		entityManager.merge( listItem);
		return true;
	}
   

}

