
package com.bharatbloodbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendWelcomeRegistrationEmail(String toEmail, String name, String role) {
        String subject = "Bharat Blood Bank - Registration Received";
        String body = String.format("""
            Dear %s,
            
            Thank you for registering on Bharat Blood Bank as a %s.
            
            Your registration is currently under review by our admin team.
            You will receive another email once your account is approved with your login credentials.
            
            This process typically takes 1-2 business days.
            
            Regards,
            Bharat Blood Bank Team
            """, name, role);
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendApprovalEmail(String toEmail, String name, String loginEmail,
                                   String temporaryPassword) {
        String subject = "Bharat Blood Bank - Account Approved ✅";
        String body = String.format("""
            Dear %s,
            
            Congratulations! Your account on Bharat Blood Bank has been APPROVED.
            
            Your login credentials:
            Email   : %s
            Password: %s
            
            Please login at: %s/api/auth/login
            
            IMPORTANT: Change your password after first login for security.
            
            Regards,
            Bharat Blood Bank Team
            """, name, loginEmail, temporaryPassword, baseUrl);
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendRejectionEmail(String toEmail, String name, String reason) {
        String subject = "Bharat Blood Bank - Registration Status";
        String body = String.format("""
            Dear %s,
            
            We regret to inform you that your registration on Bharat Blood Bank
            could not be approved at this time.
            
            Reason: %s
            
            If you believe this is an error, please contact us or re-register
            with correct documentation.
            
            Regards,
            Bharat Blood Bank Team
            """, name, reason);
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + resetToken;
        String subject = "Bharat Blood Bank - Password Reset Request";
        String body = String.format("""
            Dear %s,
            
            We received a request to reset your password.
            
            Click the link below to reset your password (valid for 15 minutes):
            %s
            
            If you did not request this, please ignore this email.
            Your password will remain unchanged.
            
            Regards,
            Bharat Blood Bank Team
            """, name, resetLink);
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendBloodRequestNotification(String bankEmail, String bankName,
                                              String doctorName, String hospital,
                                              String bloodGroup, String component,
                                              int units, boolean emergency) {
        String urgency = emergency ? "🚨 EMERGENCY" : "Normal";
        String subject = String.format("Bharat Blood Bank - New %s Blood Request", urgency);
        String body = String.format("""
            Dear %s Team,
            
            A new blood request has been submitted.
            
            Request Details:
            ----------------
            Doctor     : %s
            Hospital   : %s
            Blood Group: %s
            Component  : %s
            Units      : %d
            Priority   : %s
            
            Please login to your dashboard to review and respond.
            
            Regards,
            Bharat Blood Bank System
            """, bankName, doctorName, hospital, bloodGroup, component, units, urgency);
        sendEmail(bankEmail, subject, body);
    }

    @Async
    public void sendRequestStatusUpdateToDoctor(String doctorEmail, String doctorName,
                                                  String status, String bloodGroup,
                                                  String bankName, String notes) {
        String subject = "Bharat Blood Bank - Blood Request " + status;
        String body = String.format("""
            Dear Dr. %s,
            
            Your blood request status has been updated.
            
            Status     : %s
            Blood Group: %s
            Blood Bank : %s
            Notes      : %s
            
            Please login to your dashboard for more details.
            
            Regards,
            Bharat Blood Bank System
            """, doctorName, status, bloodGroup, bankName,
            notes != null ? notes : "N/A");
        sendEmail(doctorEmail, subject, body);
    }

    @Async
    public void sendExpiryWarningToBank(String bankEmail, String bankName,
                                         String bloodGroup, String component,
                                         long units, int daysToExpiry) {
        String subject = "⚠️ Bharat Blood Bank - Blood Expiry Warning";
        String body = String.format("""
            Dear %s Team,
            
            This is an automated alert from Bharat Blood Bank.
            
            The following blood units are expiring soon:
            
            Blood Group : %s
            Component   : %s
            Units       : %d
            Expires In  : %d days
            
            Please take necessary action to utilise these units before expiry.
            
            Regards,
            Bharat Blood Bank System
            """, bankName, bloodGroup, component, units, daysToExpiry);
        sendEmail(bankEmail, subject, body);
    }

    @Async
    public void sendDonorContactNotification(String bankEmail, String bankName,
                                              String donorName, String donorPhone,
                                              String requestId) {
        String subject = "Bharat Blood Bank - Donor Contact Initiated";
        String body = String.format("""
            Dear %s Team,
            
            A donor has been linked to Blood Request #%s.
            
            Donor Name : %s
            Donor Phone: %s
            
            Please contact the donor to arrange the donation.
            
            Regards,
            Bharat Blood Bank System
            """, bankName, requestId, donorName, donorPhone);
        sendEmail(bankEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {} | Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
