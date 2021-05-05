package org.msh.pharmadex.service.converter;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.msh.pharmadex.domain.PcCode;
import org.msh.pharmadex.service.PcCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@FacesConverter(value = "pcCodeConverter")
@Component
public class PcCodeConverter implements Converter, Serializable {
    private static final long serialVersionUID = 5821077613663099246L;
    @Autowired
    private PcCodeService pcCodeService;

    private List<PcCode> pcCodeList;

    public List<PcCode> getPcCodeList() {
        if (pcCodeList == null)
            pcCodeList = pcCodeService.getPcCodeList();
        return pcCodeList;
    }

    public PcCode findInnByName(String name) {
        for (PcCode c : getPcCodeList()) {
            if (c.getPcName().equalsIgnoreCase(name))
                return c;
        }
        return null;
    }


    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return findInnByName(submittedValue);
        } else {
            try {
                for (PcCode p : getPcCodeList()) {
                    if (p.getPcCode().equals(submittedValue)) {
                        return p;
                    }
                }
            } catch (NumberFormatException exception) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid INN Code"));
            }
        }

        return null;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            return ((PcCode) value).getPcCode();
        }
    }
}

