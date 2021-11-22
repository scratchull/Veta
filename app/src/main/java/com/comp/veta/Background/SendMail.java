package com.comp.veta.Background;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask{
    private Context context;
    private Session session;
    private String email;
    private String subject;
    private String message;
    private ProgressDialog progressDialog;

    /**
     * Constructor for the SendMail object
     * @param context : the app context
     * @param email : email to be sending to
     * @param subject : subject of the email
     * @param message : the message in the email
     */
    public SendMail(Context context, String email, String subject, String message){
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }


    /**
     * An async task that runs in the background of the app
     * gets the email of the sender and the receiver
     * Sends an email to the address of the receiver wit the given subject and message
     * @param objects : objects array to run in background
     * @return : null
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        Properties properties = new Properties(); // the properties are the setting on which the email will sent and through which medium
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.user", Config.EMAIL);


        Session session = Session.getDefaultInstance(properties); //create a session with the given properties

        Message msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(Config.EMAIL)); // from email
            InternetAddress[] toAddresses = { new InternetAddress(email) }; //set address ot the receiver
            msg.setRecipients(Message.RecipientType.TO, toAddresses); //to email
            msg.setSubject(subject); //set subject
            msg.setText(message);//set message


        } catch (MessagingException e) {
            e.printStackTrace();
        }



        // sends the e-mail
        Transport t = null;
        try {
            t = session.getTransport("smtp");
            t.connect(Config.EMAIL, Config.PASSWORD);
            t.sendMessage(msg, msg.getAllRecipients());
            t.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }



        return null;
    }
}