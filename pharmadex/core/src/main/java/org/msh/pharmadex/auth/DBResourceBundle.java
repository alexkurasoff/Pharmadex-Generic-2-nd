package org.msh.pharmadex.auth;

import javax.faces.context.FacesContext;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by usrivastava on 07/16/2014.
 */
public class DBResourceBundle extends ResourceBundle{

    protected static final String BUNDLE_NAME = DBResourceBundle.class.getName();

    public DBResourceBundle() {
        this(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public DBResourceBundle(final Locale locale) {
    	ResourceBundle parent = ResourceBundle.getBundle(BUNDLE_NAME, locale, new DBControl());
        setParent(parent);
    }

    @Override
    protected Object handleGetObject(String key) {
        return parent.getObject(key);
    }

    @Override
    public Enumeration getKeys() {
        return parent.getKeys();
    }
}
