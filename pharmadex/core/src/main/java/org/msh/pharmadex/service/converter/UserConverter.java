package org.msh.pharmadex.service.converter;

import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@FacesConverter(value = "userConverter")
@Component
public class UserConverter implements Converter, Serializable {

    private static final long serialVersionUID = -6609408645325471825L;
    @Autowired
    private UserService userService;

    private List<User> userList;

    public List<User> getUserList() {
        if (userList == null)
            userList = userService.findAllUsers();
        return userList;
    }

    public User findUserByID(String name) {
        for (User c : getUserList()) {
            if (String.valueOf(c.getUserId()).equalsIgnoreCase(name))
                return c;
        }
        return null;
    }


    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {
                System.out.println("submittedValue == " + submittedValue);
                Long number = Long.valueOf(submittedValue);
                return userService.findUser(number);
            } catch (NumberFormatException exception) {
            	return null;
            }
        }
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
    	if(value != null){
    		if(value instanceof User)
    			return String.valueOf(((User)value).getUserId());
    		if(value instanceof Long)
    			return String.valueOf(value);
    	}
    	return "";
    }
}

