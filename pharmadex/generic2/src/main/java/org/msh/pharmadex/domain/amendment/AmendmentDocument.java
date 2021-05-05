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
@Table(name="`AmendmentDocument`")
public class AmendmentDocument implements Serializable {
	private static final long serialVersionUID = 4350956686948730338L;

	public AmendmentDocument() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true)	
	private Long id;

	@Column(name="`Active`", nullable=false, length=1)	
	private boolean active = false;
	
	@Column(name="`Name`", nullable=false, length=255)	
	private String name;

	@Column(name="`Description`", nullable=true, length=255)	
	private String description;
	
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
	 * Name of the document
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Name of the document
	 */
	public String getName() {
		return name;
	}

	/**
	 * Description of document i.e. requirements to the document
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Description of document i.e. requirements to the document
	 */
	public String getDescription() {
		return description;
	}

	public String toString() {
		return String.valueOf(getId());
	}

}
