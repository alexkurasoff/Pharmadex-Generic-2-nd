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

import org.msh.pharmadex.domain.CreationDetail;
@Entity
@Table(name="`AmendmentCondition`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AmendmentCondition extends CreationDetail implements Serializable {

	private static final long serialVersionUID = -604179881906057058L;

	public AmendmentCondition() {
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
	private Long id;
	
	@Column(name="`Active`", nullable=false, length=1)	
	private boolean active = false;
	
	@Column(name="`Condition`", nullable=true, length=255)	
	private String condition;
	
	@Transient
	private int rowIndex=-1;
	
	
	/**
	 * Store of index of this record in some collection
	 * @return
	 */
	@Transient
	public int getRowIndex() {
		return rowIndex;
	}
	
	/**
	 * Store of index of this record in some collection
	 * @return
	 */
	@Transient
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public void setId(Long value) {
		this.id = value;
	}
	
	public Long getId() {
		return id;
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
	 * Condition that necessary and sufficient for this amendment formulation
	 */
	public void setCondition(String value) {
		this.condition = value;
	}
	
	/**
	 * Condition that necessary and sufficient for this amendment formulation
	 */
	public String getCondition() {
		return condition;
	}
	
	public String toString() {
		return String.valueOf(getId());
	}
	
}
