package com.utils;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class SendEmailsUtil {

    public static int sendEmail(String subject, Email fromEmail, Email toEmail, Content content) throws IOException {
        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Mail mail = new Mail(fromEmail, subject, toEmail, content);
        Request sendGridRequest = new Request();
        sendGridRequest.setMethod(Method.POST);
        sendGridRequest.setEndpoint("mail/send");
        sendGridRequest.setBody(mail.build());
        Response sendGridResponse = sg.api(sendGridRequest);
        return sendGridResponse.getStatusCode();
    }
}
