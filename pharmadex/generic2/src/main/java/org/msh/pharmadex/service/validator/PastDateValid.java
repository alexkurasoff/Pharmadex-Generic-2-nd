package org.msh.pharmadex.service.validator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Author: dudchenko
 * проверка введенной даты
 * введенная дата позже текущей - ошибка
 */
@FacesValidator("pastDateValid")
public class PastDateValid implements Validator {
	
	private final static Map<String, String> mapResource = new HashMap<String, String>();
	static{
		mapResource.put("apprDate", "error_apprdate_past");
		mapResource.put("regExpiryDate", "error_regexp_past");
	}
	
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
    	ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");
    	
    	if(o == null){
    		FacesMessage msg = new FacesMessage(resourceBundle.getString("valid_value_req"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
    	}
    	
    	String key = uiComponent.getId();
    	String messKey = mapResource.get(key);
    	
    	Date curDate = new Date();
    	Date componentDate = (Date) o;
		
		if(componentDate.before(curDate)){
			FacesMessage msg = new FacesMessage(componentDate + " " + resourceBundle.getString(messKey));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
		}
    }
}

