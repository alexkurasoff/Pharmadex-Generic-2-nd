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
@Table(name="`AmdConditions`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AmdConditions implements Serializable {

	private static final long serialVersionUID = 5306087611125843229L;

	public AmdConditions() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true)	
	private int id;

	@OneToOne(targetEntity=AmendmentCondition.class, fetch=FetchType.LAZY)	
	@JoinColumn(name="`AmendmentConditionId`", nullable=false)		
	private AmendmentCondition condition;

	@ManyToOne(targetEntity=Amendment.class, fetch=FetchType.LAZY)		
	@JoinColumn(name="`AmendmentId`")	
	@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)	
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

	public void setCondition(AmendmentCondition value) {
		this.condition = value;
	}

	public AmendmentCondition getCondition() {
		return condition;
	}

	public String toString() {
		return String.valueOf(getId());
	}

	@Embedded	
	private AmdChecklist answer;

	public AmdChecklist getAnswer()  {
		return this.answer;
	}

	public void setAnswer(AmdChecklist value)  {
		this.answer = value;
	}

}
