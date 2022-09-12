package io.project.smartcontactmanager.controller;

import io.project.smartcontactmanager.model.User;
import io.project.smartcontactmanager.repository.UserRepository;
import io.project.smartcontactmanager.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    Random random = new Random(1000);

//    Email ID form Open handler
    @RequestMapping("/forgot")
    public String openEmailForm(){
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session){

//        Generating OTP 4 digit

        System.out.println("EMAIL " + email);

        int otp = random.nextInt(999999);
        System.out.println("OTP " + otp);

//        Sending Mail
        String subject = "OTP From SCM";
        String message = "<div style = 'border: 1px solid #e2e2e2; padding: 20px"
                + "<h1>"
                + "OTP is "
                + "<b>" + otp
                + "</n>"
                + "</h1>"
                + "</div>";
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if(flag){
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        }
        else{
            session.setAttribute("message", "Check your EMAIL ID !!");

            return "forgot_email_form";
        }

    }

//    Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int otp, HttpSession session){
        int myOtp = (int) session.getAttribute("myotp");
        String email = (String)session.getAttribute("email");

        if(myOtp == otp){
//            password change form

            User user = this.userRepository.getUserByUserName(email);
            if(user == null){
//             send error message
                session.setAttribute("message", "User does not exist with this email");
                return "forgot_email_form";
            }
            else{
//              Send password change form
            }


            return "password_change_form";
        }
        else{
            session.setAttribute("message", "You have entered wrong OTP !!");
            return "verify_otp";
        }
    }
}
