package org.msh.pharmadex.domain;

import org.msh.pharmadex.util.RegistrationUtil;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Author: usrivastava
 */
@Entity
@Cacheable
@Table(name = "admin_route")
public class AdminRoute implements Serializable {
    private static final long serialVersionUID = 900102986584083067L;


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "code", length = 500, nullable = true)
    private String code;

    @Transient
    private String key;

    public String getKey() {
        String delimiter = "_";
        key = this.getClass().getSimpleName()+delimiter+ RegistrationUtil.formatString(getName());
        return key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return getName();
    }
}
