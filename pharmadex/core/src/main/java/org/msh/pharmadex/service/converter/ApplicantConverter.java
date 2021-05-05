package org.msh.pharmadex.service.converter;

import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@FacesConverter(value = "applicantConverter")
@Component
public class ApplicantConverter implements Converter, Serializable {
    @Autowired
    private ApplicantService applicantService;

    private List<Applicant> applicantList;

    public List<Applicant> getApplicantList() {
        if (applicantList == null)
            applicantList = applicantService.findAllApplicants(null);
        return applicantList;
    }

    public Applicant findApplicantByID(String name) {
        for (Applicant c : getApplicantList()) {
            if (String.valueOf(c.getApplcntId()).equalsIgnoreCase(name))
                return c;
        }
        return null;
    }


    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {
//                int number = Integer.parseInt(submittedValue);
                for (Applicant p : getApplicantList()) {
                    if (String.valueOf(p.getApplcntId()).equalsIgnoreCase(submittedValue))
                        return p;
                    if(p.getAppName().equals(submittedValue))
                    	return p;
                }
            } catch (NumberFormatException exception) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid Applicant"));
            }
        }

        return null;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

    public void setApplicantList(List<Applicant> applicantList) {
        this.applicantList = applicantList;
    }
}

