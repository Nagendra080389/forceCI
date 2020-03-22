package com.controller;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.FileNotFoundException;

public class TestMain {

    public static void main(String[] args) throws Exception {

        Email from = new Email("test@example.com");
        Email to = new Email("write2nagendra0808@gmail.com"); // use your own email address here

        String subject = "Sending with Twilio SendGrid is Fun";
        Content content = new Content("text/html", "and <em>easy</em> to do anywhere with <strong>Java</strong>");

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv("SG.k-TIw1RQQDO-ZiXfa-8k8w.Kw0r_vw-KrWZdw0T836wRjxZXjqOkePn2apT7rGTwPU"));
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        System.out.println(response.getStatusCode());
        System.out.println(response.getHeaders());
        System.out.println(response.getBody());
    }

    public void execute() throws FileNotFoundException {

    }
}
