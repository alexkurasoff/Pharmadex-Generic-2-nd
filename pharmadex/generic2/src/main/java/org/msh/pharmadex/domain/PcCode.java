package org.msh.pharmadex.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Cacheable
@Table(name = "pccode")
public class PcCode extends CreationDetail implements Serializable
{
    @Id
    @Column(nullable = false, unique = true)
   	private String pcCode;

    @Column(length = 500, nullable = false)
    private String pcName;

   	@ManyToOne
   	@JoinColumn(name="PARENT_ID")
   	private PcCode parent;

   	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY)
   	private List<PcCode> units = new ArrayList<PcCode>();

    @Column(nullable=false)
   	private int level;

    @ManyToMany(targetEntity = Product.class, fetch = FetchType.LAZY)
    @JoinTable(name = "prod_pccode", joinColumns = @JoinColumn(name = "pccode_id"), inverseJoinColumns = @JoinColumn(name = "prod_id"))
    private List<Product> products;

   	/**
     * Static method that return the parent code of a given code
     * @param code
     * @return
     */
    public static String getParentCode(String code) {
        if ((code == null) || (code.length() <= 3))
            return null;

        if (code.length() <= 6)
            return code.substring(0, 3);
        if (code.length() <= 9)
            return code.substring(0, 6);
        if (code.length() <= 12)
            return code.substring(0, 9);
        return code.substring(0, 12);
    }

    /**
     * Static method to check if a code is equals of a child of the code given by the parentCode param
     *
     * @param parentCode
     * @param code
     * @return
     */
    public static boolean isSameOrChildCode(String parentCode, String code) {
        int len = parentCode.length();
        if (len > code.length())
            return false;
        return (parentCode.equals(code.substring(0, parentCode.length())));
    }

    /**
     * Return the parent list including the own object
   	 * @return List of {@link PcCode} instance
   	 */
   	public List<PcCode> getParents() {
   		return getParentsTreeList(true);
   	}

   	/**
   	 * Return the display name of the Atc concatenated with its parent
   	 * @return
   	 */
   	public String getFullDisplayName() {
   		String s = getPcName().toString();

   		for (PcCode adm: getParentsTreeList(false)) {
   			s += ", " + adm.getPcName().toString();
   		}

   		return s;
   	}
   	
   	/**
   	 * AtcCode AtcName
   	 */
   	public String getDisplayName() {
   		String s = getPcCode() + " " + getPcName();
   		return s;
   	}

    /**
   	 * Return a list with parents atc, where the first is the upper level ATC and
   	 * the last the lowest level
   	 * @return {@link List} of {@link PcCode} instances
   	 */
   	public List<PcCode> getParentsTreeList(boolean includeThis) {
   		ArrayList<PcCode> lst = new ArrayList<PcCode>();

   		PcCode aux;
   		if (includeThis)
   			 aux = this;
   		else aux = getParent();

   		while (aux != null) {
   			lst.add(0, aux);
   			aux = aux.getParent();
   		}
   		return lst;
   	}

   	/**
   	 * Return the parent atc based on its level. If level is the same of this unit, it returns itself.
   	 * If level is bigger than the level of this unit, it returns null
   	 * @param level
   	 * @return {@link PcCode} instance, which is itself or a parent admin unit
   	 */
   	public PcCode getAdminUnitByLevel(int level) {
   		List<PcCode> lst = getParents();
   		for (PcCode adm: lst) {
   			if (adm.getLevel()== level)
   				return adm;
   		}
   		return null;
   	}


   	/**
   	 * Return parent administrative unit of level 1
   	 * @return {@link PcCode} instance
   	 */
   	public PcCode getParentLevel1() {
   		return getAdminUnitByLevel(1);
   	}


   	/**
   	 * Return parent administrative unit of level 2
   	 * @return {@link PcCode} instance
   	 */
   	public PcCode getParentLevel2() {
   		return getAdminUnitByLevel(2);
   	}


   	/**
   	 * Return parent administrative unit of level 3
   	 * @return {@link PcCode} instance
   	 */
   	public PcCode getParentLevel3() {
   		return getAdminUnitByLevel(3);
   	}


   	/**
   	 * Return parent administrative unit of level 4
   	 * @return {@link PcCode} instance
   	 */
   	public PcCode getParentLevel4() {
   		return getAdminUnitByLevel(4);
   	}


   	/**
   	 * Return parent administrative unit of level 5
   	 * @return {@link PcCode} instance
   	 */
   	public PcCode getParentLevel5() {
   		return getAdminUnitByLevel(5);
   	}

    public String getPcCode() {
        return pcCode;
    }

    public void setPcCode(String pcCode) {
        this.pcCode = pcCode;
    }

    public String getPcName() {
        return pcName;
    }

    public void setPcName(String pcName) {
        this.pcName = pcName;
    }

    public PcCode getParent() {
        return parent;
    }

    public void setParent(PcCode parent) {
        this.parent = parent;
    }

    public List<PcCode> getUnits() {
        return units;
    }

    public void setUnits(List<PcCode> units) {
        this.units = units;
    }

   	/* (non-Javadoc)
   	 * @see java.lang.Object#toString()
   	 */
   	@Override
   	public String toString() {
   		return getFullDisplayName();
   	}

   	/* (non-Javadoc)
   	 * @see java.lang.Object#equals(java.lang.Object)
   	 */
   	@Override
   	public boolean equals(Object obj) {
   		if (obj == this)
   			return true;

   		if (!(obj instanceof PcCode))
   			return false;

   		return ((PcCode)obj).getPcCode().equals(getPcCode());
   	}

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
