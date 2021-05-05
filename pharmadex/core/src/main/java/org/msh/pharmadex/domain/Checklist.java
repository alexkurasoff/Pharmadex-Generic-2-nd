package org.msh.pharmadex.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Cacheable
@Table(name = "checklist")
public class Checklist extends CreationDetail implements Serializable {
    private static final long serialVersionUID = -6121362181619288650L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
    private Long id;

    @Column(name = "name", length = 500)
    private String name;

    private String module;

    private String moduleNo;

    @Column(name = "header")
    private boolean header;

    @Column(name = "new_med")
    private boolean newMed;

    @Column(name = "gen_med")
    private boolean genMed;

    @Column(name = "recognized_med")
    private boolean recognizedMed;

    @Column(name = "renewal")
    private Boolean renewal;
    
    @Column(name = "variation")
    private Boolean variation;

    public Boolean isVariation() {
		return variation;
	}

	public void setVariation(Boolean variation) {
		this.variation = variation;
	}

    @Column(name = "majvar")
    private Boolean majvar;

    public Boolean getVariation() {
        return variation;
    }

    public Boolean getMajvar() {
        return majvar;
    }

    public void setMajvar(Boolean majvar) {
        this.majvar = majvar;
    }

    public Boolean getRenewal() {
        return renewal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModuleNo() {
        return moduleNo;
    }

    public void setModuleNo(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isNewMed() {
        return newMed;
    }

    public void setNewMed(boolean newMed) {
        this.newMed = newMed;
    }

    public boolean isGenMed() {
        return genMed;
    }

    public void setGenMed(boolean genMed) {
        this.genMed = genMed;
    }

    public boolean isRecognizedMed() {
        return recognizedMed;
    }

    public void setRecognizedMed(boolean recognizedMed) {
        this.recognizedMed = recognizedMed;
    }

    public Boolean isRenewal() {
        return renewal;
    }

    public void setRenewal(Boolean renewal) {
        this.renewal = renewal;
    }
}
