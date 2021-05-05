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

import javax.persistence.*;

import org.msh.pharmadex.domain.enums.YesNoNA;
/**
 * ORM-Component Class
 */
@Embeddable
public class AmdChecklist {
	public AmdChecklist() {
	}
	
	@Column(name="`answer_value`", nullable=true, length=16)	
	@Enumerated(EnumType.STRING)
    private YesNoNA value;
	
	/**
	 * Applicant's value (yes or no) maybe na
	 */
	public void setValue(YesNoNA value) {
		this.value = value;
	}
	
	/**
	 * Applicant's value (yes or no) maybe na
	 */
	public YesNoNA getValue() {
		return value;
	}
	
	@Column(name="`answer_staffValue`", nullable=true, length=16)	
	@Enumerated(EnumType.STRING)
    private YesNoNA staffValue;
	
	/**
	 * Staff value (yes or no or na)
	 */
	public void setStaffValue(YesNoNA value) {
		this.staffValue = value;
	}
	
	/**
	 * Staff value (yes or no or na)
	 */
	public YesNoNA getStaffValue() {
		return staffValue;
	}
	
	@Column(name="`answer_staffComment`", nullable=true, length=1024)	
	private String staffComment;
	
	/**
	 * Staff comment
	 */
	public void setStaffComment(String value) {
		this.staffComment = value;
	}
	
	/**
	 * Staff comment
	 */
	public String getStaffComment() {
		return staffComment;
	}
	
}
