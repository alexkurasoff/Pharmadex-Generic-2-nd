package org.msh.pharmadex.mbean;

import org.msh.pharmadex.domain.Letter;
import org.msh.pharmadex.service.LetterService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@RequestScoped
public class LetterMbn implements Serializable {

    @ManagedProperty(value = "#{letterService}")
    LetterService letterService;
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
    private List<Letter> letters;
    private Letter selLetter = new Letter();
    private Long selLetterId;


    public void addLetter() {
        if (selLetter == null)
            selLetter = new Letter();
        System.out.println("" + selLetter.getBody());
        String result = letterService.addLetter(selLetter);
        if (result.equalsIgnoreCase("persisted")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("global.success"), MessageFormat.format(bundle.getString("create_success"), selLetter.getTitle())));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), "There was an error creating " + selLetter.getTitle()));
        }
        selLetter = new Letter();
    }

    public void cancel() {
        selLetter = new Letter();
    }


    public List<Letter> getLetters() {
        if (letters == null)
            letters = letterService.getLetters();
        return letters;
    }

    public void setLetters(List<Letter> letters) {
        this.letters = letters;
    }

    public Letter getSelLetter() {
        return selLetter;
    }

    public void setSelLetter(Letter selLetter) {
        this.selLetter = selLetter;
    }

    public Long getSelLetterId() {
        return selLetterId;
    }

    public void setSelLetterId(Long selLetterId) {
        this.selLetterId = selLetterId;
    }

    public LetterService getLetterService() {
        return letterService;
    }

    public void setLetterService(LetterService letterService) {
        this.letterService = letterService;
    }
}