package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.exception.customException.InvalidOtpEx;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private String otpCache;
    private Session session;

    public void init(){
        Properties prop = new Properties();
        prop.put("mail.smtp.host","smtp.gmail.com");
        prop.put("mail.smtp.port","465");
        prop.put("mail.smtp.auth","true");
        prop.put("mail.smtp.ssl.enable","true");
        prop.put("mail.smtp.ssl.trust","*");
        Authenticator authenticator=new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("techsoftindia321@gmail.com","pzei pmxa ylhw xmzl");
            }
        };
        this.session=Session.getInstance(prop,authenticator);
    }
    public String generateOtp(){
        return new DecimalFormat("000000").format(new SecureRandom().nextInt(999999));
    }

    @Async(value = "taskExecutor")
    public void sendOtp(String to) throws MessagingException {

        MimeMessage mimeMessage=new MimeMessage(session);
        String otp = generateOtp();
        mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
        mimeMessage.setContent(getEmailBody(otp), "text/html");
//        Transport.send(mimeMessage);
        log.info("otp : {}",otp);
        otpCache=otp;
    }

    public void sendResetLink(String to,String resetLink) throws MessagingException {
        MimeMessage mimeMessage=new MimeMessage(session);
        mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
        mimeMessage.setContent(getResetPasswordBody(resetLink), "text/html");
        Transport.send(mimeMessage);
    }


    public String getEmailBody(String otp){
        String body= "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #2c3e50; text-align: center; color: #ffffff;\">\n" +
                "    <table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width: 600px; background-color: #34495e; margin: 20px auto; border-radius: 8px; box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);\">\n" +
                "        <tr>\n" +
                "            <td style=\"padding: 20px; text-align: center;\">\n" +
                "                <h1 style=\"color: #ecf0f1; margin-bottom: 20px; font-size: 28px;\">OTP Verification</h1>\n" +
                "                <p style=\"font-size: 16px; color: #bdc3c7; line-height: 1.5;\">Your One-Time Password (OTP) is:</p>\n" +
                "                <h2 style=\"font-size: 36px; color: #1abc9c; margin: 20px 0; font-weight: bold; letter-spacing: 2px;\">{{otp}}</h2>\n" +
                "                <p style=\"font-size: 16px; color: #bdc3c7; line-height: 1.5;\">Please enter this OTP in the application to complete your verification. This code is valid for 10 minutes.</p>\n" +
                "                <p style=\"font-size: 14px; color: #95a5a6; margin-top: 20px; font-style: italic;\">If you did not request this OTP, please ignore this email.</p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td style=\"background-color: #2c3e50; padding: 10px; text-align: center;\">\n" +
                "                <p style=\"font-size: 14px; color: #95a5a6;\">&copy; 2024 Swetlox. All rights reserved.</p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n";
        return body.replace("{{otp}}",otp);
    }

    public String getResetPasswordBody(String resetLink){
        String body = "<body style=\"font-family: Arial, sans-serif; background-color: #2c3e50; margin: 0; padding: 0; color: #ffffff;\">\n" +
                "    <div style=\"max-width: 600px; margin: 50px auto; background-color: #34495e; padding: 30px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\">\n" +
                "        <h2 style=\"color: #ecf0f1; text-align: center; font-size: 24px; margin-bottom: 20px;\">Reset Your Password</h2>\n" +
                "        <p style=\"font-size: 16px; color: #bdc3c7; text-align: center;\">Hello,</p>\n" +
                "        <p style=\"font-size: 16px; color: #bdc3c7; text-align: center;\">We received a request to reset your password. Please click the button below to reset your password:</p>\n" +
                "        <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "            <a href=\"{{resetLink}}\" style=\"background-color: #1abc9c; color: #ffffff; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-size: 18px; font-weight: bold; text-transform: uppercase;\">Reset Password</a>\n" +
                "        </div>\n" +
                "        <p style=\"font-size: 16px; color: #bdc3c7; text-align: center;\">If you did not request a password reset, please ignore this email or contact support if you have any questions.</p>\n" +
                "        <hr style=\"border: 0; border-top: 1px solid #7f8c8d; margin: 20px 0;\">\n" +
                "        <p style=\"font-size: 14px; color: #95a5a6; text-align: center;\">If the button above doesn’t work, copy and paste the following link into your browser:</p>\n" +
                "        <p style=\"font-size: 14px; color: #1abc9c; text-align: center; word-wrap: break-word;\">\n" +
                "            <a href=\"{{resetLink}}\" style=\"color: #1abc9c; text-decoration: none;\">{{resetLink}}</a>\n" +
                "        </p>\n" +
                "        <p style=\"font-size: 12px; color: #95a5a6; text-align: center; margin-top: 20px;\">This link will expire in 30 minutes.</p>\n" +
                "        <p style=\"font-size: 12px; color: #95a5a6; text-align: center;\">Thank you,</p>\n" +
                "        <p style=\"font-size: 12px; color: #95a5a6; text-align: center;\">The Swetlox Team</p>\n" +
                "    </div>\n" +
                "</body>\n";
        return body.replace("{{resetLink}}",resetLink);
    }
    public void validateOtp(String otp) throws InvalidOtpEx {
        if(!otp.equals(otpCache)){
            throw new InvalidOtpEx("invalid otp "+otp);
        }
    }

    public void clearCache(){
        otpCache=null;
    }
}
