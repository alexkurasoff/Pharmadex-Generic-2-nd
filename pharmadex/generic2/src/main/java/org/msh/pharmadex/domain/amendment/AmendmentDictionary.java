/**
 * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
 * 
 * This is an automatic generated file. It will be regenerated every time 
 * you generate persistence class.
 * 
 * Modifying its content may cause the program not work, or your work may lost.
 */

/**
 * Licensee: DuKe TeAm
 * License Type: Purchased
 */
package org.msh.pharmadex.domain.amendment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.msh.pharmadex.domain.CreationDetail;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.AmendmentSubject;
import org.msh.pharmadex.domain.enums.PharmadexDB;
/**
 * This class contains all configured amendments
 */
@Entity
@Table(name="`AmendmentDictionary`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AmendmentDictionary extends CreationDetail implements Serializable {

	private static final long serialVersionUID = 7568551476100645376L;

	public AmendmentDictionary() {
	}
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true)	
	private Long id;

	@Column(name="`Formulation`", nullable=true, length=1024)	
	private String formulation;

	@Column(name="`Process`", nullable=false, length=20)
	@Enumerated(EnumType.STRING)
	private AmendmentProcess process;

	@Column(name="`Chapter`", nullable=true, length=255)	
	private String chapter;

	@Column(name="`SubChapter`", nullable=true, length=255)	
	private String subChapter;

	@Column(name="`PharmadexDB`", nullable=true, length=20)
	@Enumerated(EnumType.STRING)
	private PharmadexDB pharmadexDB;
	
	@Column(name="`Subject`", nullable=true, length=20)
	@Enumerated(EnumType.STRING)
	private AmendmentSubject subject;
	
	@Column(name="`Active`", nullable=false, length=1)	
	private boolean active = false;

	@OneToMany(targetEntity=AmendmentCondition.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)	
	@JoinColumn(name="`AmendmentDictionaryID`", nullable=true)	
	private List<AmendmentCondition> amendmentConditions = new ArrayList<AmendmentCondition>();

	@OneToMany(targetEntity=AmendmentDocument.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)	
	@JoinColumn(name="`AmendmentDictionaryID`", nullable=true)	
	private List<AmendmentDocument> amendmentDocuments = new ArrayList<AmendmentDocument>();



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Amendment formulation, like "A.1 Change in the name and/or address of the marketing authorisation holder" No more then 20 words
	 */
	public void setFormulation(String value) {
		this.formulation = value;
	}

	/**
	 * Amendment formulation, like "A.1 Change in the name and/or address of the marketing authorisation holder" No more then 20 words
	 */
	public String getFormulation() {
		return formulation;
	}

	/**
	 * Process type for this variation. Typical values "minor" and "major". Sometimes "notification" and "approval"
	 */
	public AmendmentProcess getProcess() {
		return process;
	}
	/**
	 * Process type for this variation. Typical values "minor" and "major". Sometimes "notification" and "approval"
	 */
	public void setProcess(AmendmentProcess process) {
		this.process = process;
	}
	/**
	 * Subject to an amendment. May be Product or Applicant or ...
	 */
	public AmendmentSubject getSubject() {
		return subject;
	}
	/**
	 * Subject to an amendment. May be Product or Applicant or ...
	 */
	public void setSubject(AmendmentSubject subject) {
		this.subject = subject;
	}

	/**
	 * Chapter to easy find a variation, for example "Administrative changes"
	 */
	public void setChapter(String value) {
		this.chapter = value;
	}

	/**
	 * Chapter to easy find a variation, for example "Administrative changes"
	 */
	public String getChapter() {
		return chapter;
	}

	/**
	 * For Quality chapter should be defined sub-chapters, like "manufacture, control of active substance" etc
	 */
	public void setSubChapter(String value) {
		this.subChapter = value;
	}

	/**
	 * For Quality chapter should be defined sub-chapters, like "manufacture, control of active substance" etc
	 */
	public String getSubChapter() {
		return subChapter;
	}
	/**
	 * Which part of Pharmadex database is impacted
	 */
	public PharmadexDB getPharmadexDB() {
		return pharmadexDB;
	}
	/**
	 * Which part of Pharmadex database is impacted
	 */
	public void setPharmadexDB(PharmadexDB pharmadexDB) {
		this.pharmadexDB = pharmadexDB;
	}
	
	/**
	 * Is this Amendment active?
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Is this Amendment active?
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * List of required conditions
	 */
	public List<AmendmentCondition> getAmendmentConditions() {
		if(amendmentConditions != null){
			int i=0;
			for(AmendmentCondition item : amendmentConditions){
				item.setRowIndex(i++);
			}
		}
		return amendmentConditions;
	}
	/**
	 * List of required conditions
	 */
	public void setAmendmentConditions(List<AmendmentCondition> amendmentConditions) {
		this.amendmentConditions = amendmentConditions;
	}
	/**
	 * List of required documents
	 */
	public List<AmendmentDocument> getAmendmentDocuments() {
		if(amendmentDocuments != null){
			int i=0;
			for(AmendmentDocument item : amendmentDocuments){
				item.setRowIndex(i++);
			}
		}
		return amendmentDocuments;
	}
	/**
	 * List of required documents
	 */
	public void setAmendmentDocuments(List<AmendmentDocument> amendmentDocuments) {
		this.amendmentDocuments = amendmentDocuments;
	}

	public String toString() {
		return getId().toString();
	}

}
