package org.msh.pharmadex.util;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.dao.iface.ErrorLogDAO;
import org.msh.pharmadex.domain.ErrorLog;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.el.ELContext;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewExpiredExceptionExceptionHandler extends ExceptionHandlerWrapper {
    private static final Logger log = Logger.getLogger(ViewExpiredExceptionExceptionHandler.class.getCanonicalName());
    private ExceptionHandler wrapped;

    public ViewExpiredExceptionExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {

        final Iterator<ExceptionQueuedEvent> b = getUnhandledExceptionQueuedEvents().iterator();
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext(); ) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

            final FacesContext facesContext = FacesContext.getCurrentInstance();
            final ExternalContext ec = facesContext.getExternalContext();
            final Map<String, Object> requestMap = ec.getRequestMap();
            final NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
            HttpServletRequest request = (HttpServletRequest) ec.getRequest();
            Throwable t = context.getException();

            //email notifications

            WebApplicationContext ctx = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(request.getServletContext());  // (1) get ctx using the WebApplicationContextUtils class

            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            UserSession userSession
                    = (UserSession) FacesContext.getCurrentInstance().getApplication()
                    .getELResolver().getValue(elContext, null, "userSession");
            try {
                ErrorLogDAO errorLogDAO = (ErrorLogDAO) ctx.getBean("errorLogDAO");

                ErrorLog errorLog = new ErrorLog();
                errorLog.setBrowserName(JsfUtils.getBrowserName(ec.getRequestHeaderMap().get("User-Agent")));
                errorLog.setUserIP(request.getRemoteAddr());
                errorLog.setRequestURI(request.getRequestURI());
                errorLog.setAjaxRequest("" + facesContext.getPartialViewContext().isAjaxRequest());
                errorLog.setErrorDate(new Date());
                errorLog.setExceptionMessage(t.getMessage());
                errorLog.setExceptionType(t.getClass().getCanonicalName());
                StringWriter errors = new StringWriter();
                t.printStackTrace(new PrintWriter(errors));
                errorLog.setStackTrace(errors.toString().substring(0, 255));
                errorLogDAO.save(errorLog);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

           if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                try {
                    // Push some useful stuff to the request scope for use in the page
                	
                	//t.getStackTrace();
                	if(t.getMessage()!=null && !t.getMessage().contains("home.faces"))
                		log.log(Level.SEVERE, "ViewExpiredException!", t);
                    //redirect home page
                    requestMap.put("currentViewId", vee.getViewId());
                    String url = facesContext.getExternalContext().getRequestContextPath();
					facesContext.getExternalContext().redirect(url + "/home.faces");
                } catch (IOException e) {
					e.printStackTrace();
                } finally {
                    i.remove();
                }
            }else if(t instanceof FacesException) {
            	FacesException vee = (FacesException) t;
                try {
                	log.log(Level.SEVERE, "FacesException!", t);
                    //redirect home page
                    requestMap.put("currentViewId", vee.getMessage());
                	String url = facesContext.getExternalContext().getRequestContextPath();
					facesContext.getExternalContext().redirect(url + "/home.faces");
				} catch (IOException e) {
					e.printStackTrace();
                } finally {
                    i.remove();
                }
			}else if(t instanceof ELException){
				try {
					log.log(Level.SEVERE, "ELException!", t);
                    //redirect home page
                    requestMap.put("currentViewId", t.getMessage());
		    		String url = facesContext.getExternalContext().getRequestContextPath();
		    		facesContext.getExternalContext().redirect(url + "/home.faces");
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
                    i.remove();
                }
			}else {
                //log error ?
                log.log(Level.SEVERE, "Critical Exception!", t);
                //redirect error page
                requestMap.put("exceptionMessage", t.getMessage());
                //navigationHandler.handleNavigation(facesContext, null, "/pages/error.faces");
                navigationHandler.handleNavigation(facesContext, null, "/home.faces");
                facesContext.renderResponse();
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents. Therefore, let the parent handle them.
        getWrapped().handle();
    }
}
