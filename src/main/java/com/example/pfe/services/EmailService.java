package com.example.pfe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("kmn242424@gmail.com")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            String subject = "Vérification de votre compte";

            String htmlContent = "<p>Bonjour,</p>"
                    + "<p>Veuillez cliquer sur le lien suivant pour vérifier votre compte :</p>"
                    + "<p><a href=\"" + verificationLink + "\">Vérifier mon compte</a></p>"
                    + "<p>Merci !</p>"
                    + "<hr>"
                    + "<p><strong>Équipe administrative </strong><br>Email : contact@store.tn</p>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }

    // public void sendContactEmail(String toEmail, String subject, String body) {
    //     MimeMessage message = mailSender.createMimeMessage();

    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    //         helper.setFrom(fromEmail);
    //         helper.setTo(toEmail);
    //         helper.setSubject(subject);
    //         helper.setText(body, true); // you can support HTML too
    //         mailSender.send(message);
    //     } catch (MessagingException e) {
    //         e.printStackTrace();
    //     }
    // }
}
