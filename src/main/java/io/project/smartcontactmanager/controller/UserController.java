package io.project.smartcontactmanager.controller;


import io.project.smartcontactmanager.model.Contact;
import io.project.smartcontactmanager.model.User;
import io.project.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

//    Har bar chal jayega
    @ModelAttribute
    public void addCommonData(Model model, Principal principal){
        String userName = principal.getName();
        System.out.println(userName);
        User user = userRepository.getUserByUserName(userName);

        System.out.println(user);
        model.addAttribute("user", user);
    }
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){

        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

//    Open form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

//    processing Add Contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("processImage") MultipartFile file, Principal principal){

        try{
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);


//            processing and uploading file
            if(file.isEmpty()){
//                if the file is empty, produce a message
                System.out.println("File is empty");
            }else{
//                copy the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded!");
            }
            contact.setUser(user);
            user.getContacts().add(contact);

            this.userRepository.save(user);

            System.out.println("Date " + contact);
            System.out.println("Added to Database");
        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        return "normal/add_contact_form";
    }
}
