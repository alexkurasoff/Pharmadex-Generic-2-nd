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
import javax.persistence.*;
@Entity
@Table(name="`AmdDocuments`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AmdDocuments implements Serializable {
	public AmdDocuments() {
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true)
	private int id;
	
	@OneToOne(targetEntity=AmendmentDocument.class, fetch=FetchType.LAZY)	
	@JoinColumn(name="`AmendmentDocumentId`")	
	private org.msh.pharmadex.domain.amendment.AmendmentDocument document;
	
	@ManyToOne(targetEntity=Amendment.class, fetch=FetchType.LAZY)		
	@JoinColumn(name="`AmendmentId`")
	private org.msh.pharmadex.domain.amendment.Amendment amendment;
	
	public void setId(int value) {
		this.id = value;
	}
	
	public int getId() {
		return id;
	}
	
	public void setAmendment(Amendment value) {
		this.amendment = value;
	}
	
	public Amendment getAmendment() {
		return amendment;
	}
	
	public void setDocument(AmendmentDocument value) {
		this.document = value;
	}
	
	public AmendmentDocument getDocument() {
		return document;
	}
	
	public String toString() {
		return String.valueOf(getId());
	}
	
	@Embedded	
	private org.msh.pharmadex.domain.amendment.AmdChecklist answer;
	
	public org.msh.pharmadex.domain.amendment.AmdChecklist getAnswer()  {
		if(this.answer == null){
			setAnswer(new AmdChecklist());
		}
		return this.answer;
	}
	
	public void setAnswer(AmdChecklist value)  {
		this.answer = value;
	}
	
}
