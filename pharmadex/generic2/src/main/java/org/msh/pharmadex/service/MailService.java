package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.msh.pharmadex.dao.iface.MailDAO;
import org.msh.pharmadex.domain.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService implements Serializable {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private SimpleMailMessage alertMailMessage;

    @Autowired
    private MailDAO mailDAO;

    @Autowired
    private LetterService letterService;

    public void sendMail(Mail mailObj, boolean saveMail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("usrivastava@msh.org");
        message.setTo(mailObj.getMailto());
        message.setSubject(mailObj.getSubject());
        message.setText(mailObj.getMessage());
        message.setSentDate(mailObj.getDate());

        mailSender.send(message);
        if (saveMail)
            mailDAO.saveAndFlush(mailObj);
    }

    public void sendMailFromSender(Mail mailObj, boolean saveMail, String sender) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(mailObj.getMailto());
        message.setSubject(mailObj.getSubject());
        message.setText(mailObj.getMessage());
        message.setSentDate(mailObj.getDate());

        mailSender.send(message);
        if (saveMail)
            mailDAO.saveAndFlush(mailObj);
    }

    public String sendMailWithHTML(Mail mailObj, boolean saveMail, String from) throws MessagingException {
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        MimeMessage message = sender.createMimeMessage();

        // use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = null;

        helper = new MimeMessageHelper(message, false);
       // message.setContent(htmlMsg, "text/html");
        
        helper.setTo(mailObj.getMailto());
        helper.setText(mailObj.getMessage(), true);
        helper.setSubject(mailObj.getSubject());
        helper.setFrom(from);
        sender.send(helper.getMimeMessage());

        if (saveMail)
            mailDAO.saveAndFlush(mailObj);
        return "mail_sent";
    }
    
    public String sendMailWithAttach(Mail mailObj, boolean saveMail, byte[] attach) throws MessagingException {
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        MimeMessage message = sender.createMimeMessage();

        // use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = null;

        helper = new MimeMessageHelper(message, true);
        helper.setTo(mailObj.getMailto());
        helper.setText(mailObj.getMessage());
        helper.setSubject(mailObj.getSubject());
        helper.addAttachment("invoice.pdf", new ByteArrayResource(attach));
        sender.send(helper.getMimeMessage());

        if (saveMail)
            mailDAO.saveAndFlush(mailObj);
        return "mail_sent";
    }

    public void sendAlertMail(String alert) {
        SimpleMailMessage mailMessage = new SimpleMailMessage(alertMailMessage);
        mailMessage.setText(alert);
        mailSender.send(mailMessage);
    }

    public List<Mail> findAllMailSent(Long prodAppId) {
        return mailDAO.findByProdApplications_IdOrderByDateDesc(prodAppId);
    }

}


