package org.msh.pharmadex.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.msh.pharmadex.domain.enums.TemplateType;

/**
 * Storage for file templates that Administrator can download
 * @author Alex Kurasoff
 *
 */
@Entity
@Table(name = "filetemplate")
public class FileTemplate {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(length=32)
	@Enumerated(EnumType.STRING)
	private TemplateType templateType;
	
	@Column(length = 500, nullable = true)
	private String contentType;

	@Column(length = 500, nullable = true)
	private String fileName;

	@Lob
	@Column(nullable = true)
	private byte[] file;

	@Column(length = 1)	
	private Boolean staffOnly;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Unique type of template
	 * @return
	 */
	public TemplateType getTemplateType() {
		return templateType;
	}
	/**
	 * Unique type of template
	 */
	public void setTemplateType(TemplateType templateType) {
		this.templateType = templateType;
	}

	/**
	 * Content type to transfer (internal)
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * Content type to transfer (internal)
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Name of template and file result
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * Name of template and file result
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * The file
	 * @return
	 */
	public byte[] getFile() {
		return file;
	}
	/**
	 * The file
	 */
	public void setFile(byte[] file) {
		this.file = file;
	}

	/** visible Letter only staff */
	public Boolean getStaffOnly() {
		return staffOnly;
	}

	/** visible Letter only staff */
	public void setStaffOnly(Boolean staffOnly) {
		this.staffOnly = staffOnly;
	}
}
