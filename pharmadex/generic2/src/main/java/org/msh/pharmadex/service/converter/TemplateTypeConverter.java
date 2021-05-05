package org.msh.pharmadex.service.converter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.msh.pharmadex.domain.enums.TemplateType;
import org.springframework.stereotype.Component;

@FacesConverter(value = "templateTypeConverter")
@Component
public class TemplateTypeConverter implements Converter, Serializable {

	private static final long serialVersionUID = -4589803987254612453L;
    
    public List<TemplateType> getAllTemplateType(){		
		return Arrays.asList(TemplateType.values());
    }

    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
    	TemplateType templateType = null;
    	if (submittedValue.trim().equals("")) {
            return templateType;
        } else {      
        	List<TemplateType>  allTemplateType = getAllTemplateType() ;
        	for(TemplateType tt :allTemplateType){
        		if(tt.name().equals(submittedValue)){
        			templateType = tt;
        			return templateType;        			
        		}
        	}
        	return templateType; 
        }       
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || "".equals(value) || ("null".equals(value))) {
            return null;
        } else {
        	if (value instanceof TemplateType) {
        		 return  ((TemplateType) value).name();
            }
        }
        return null;
    }
}
