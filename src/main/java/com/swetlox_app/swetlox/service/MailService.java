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
    private Properties prop;
    private Session session;

    @PostConstruct
    public void init(){
        prop=new Properties();
        prop.put("mail.smtp.host","smtp.gmail.com");
        prop.put("mail.smtp.port","465");
        prop.put("mail.smtp.auth","true");
        prop.put("mail.smtp.ssl.enable","true");
        prop.put("mail.smtp.ssl.trust","*");
        Authenticator authenticator=new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("********@gmail.com","*******");
            }
        };
        session=Session.getInstance(prop,authenticator);
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
        Transport.send(mimeMessage);
        log.info("otp : {}",otp);
        otpCache=otp;
    }

    public void sendResetLink(String to,String resetLink) throws MessagingException {
        MimeMessage mimeMessage=new MimeMessage(session);
        String otp = generateOtp();
        mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
        mimeMessage.setContent(getResetPasswordBody(resetLink), "text/html");
//        Transport.send(mimeMessage);
        log.info("otp : {}",otp);
    }


    public String getEmailBody(String otp){
        return "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; text-align: center;\">\n" +
                "    <table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width: 600px; background-color: #ffffff; margin: 20px auto; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">\n" +
                "        <tr>\n" +
                "            <td style=\"padding: 20px; text-align: center;\">\n" +
                "                <h1 style=\"color: #333333; margin-bottom: 20px;\">OTP Verification</h1>\n" +
                "                <p style=\"font-size: 16px; color: #666666;\">Your One-Time Password (OTP) is:</p>\n" +
                "                <h2 style=\"font-size: 32px; color: #4CAF50; margin: 20px 0;\">" + otp + "</h2>\n" +
                "                <p style=\"font-size: 16px; color: #666666;\">Please enter this OTP in the application to complete your verification. This code is valid for 10 minutes.</p>\n" +
                "                <p style=\"font-size: 14px; color: #999999; margin-top: 20px;\">If you did not request this OTP, please ignore this email.</p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td style=\"background-color: #f9f9f9; padding: 10px; text-align: center;\">\n" +
                "                <p style=\"font-size: 14px; color: #999999;\">&copy; 2024 Swetlox. All rights reserved.</p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>";
    }

    public String getResetPasswordBody(String resetLink){
        return "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;\">\n" +
                "    <div style=\"max-width: 600px; margin: 50px auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\">\n" +
                "        <h2 style=\"color: #333333; text-align: center;\">Reset Your Password</h2>\n" +
                "        <p style=\"font-size: 16px; color: #555555; text-align: center;\">Hello,</p>\n" +
                "        <p style=\"font-size: 16px; color: #555555; text-align: center;\">We received a request to reset your password. Click the button below to reset your password:</p>\n" +
                "        <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "            <a href=\""+ resetLink  +"\" style=\"background-color: #007bff; color: #ffffff; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-size: 16px;\"> reset link </a>\n" +
                "        </div>\n" +
                "        <p style=\"font-size: 16px; color: #555555; text-align: center;\">If you did not request a password reset, please ignore this email or contact support if you have questions.</p>\n" +
                "        <hr style=\"border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;\">\n" +
                "        <p style=\"font-size: 14px; color: #aaaaaa; text-align: center;\">If the button above doesn’t work, copy and paste the following link into your browser:</p>\n" +
                "        <p style=\"font-size: 14px; color: #007bff; text-align: center;\"><a href=\""+ resetLink +"\" style=\"color: #007bff; text-decoration: none;\">"+ resetLink +"</a></p>\n" +
                "        <p style=\"font-size: 12px; color: #aaaaaa; text-align: center;\">This link will expire in 30 minutes.</p>\n" +
                "        <p style=\"font-size: 12px; color: #aaaaaa; text-align: center;\">Thank you,</p>\n" +
                "        <p style=\"font-size: 12px; color: #aaaaaa; text-align: center;\">The Swetlox Team</p>\n" +
                "    </div>\n" +
                "</body>";
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
