package io.project.smartcontactmanager.controller;


import io.project.smartcontactmanager.helper.Message;
import io.project.smartcontactmanager.model.Contact;
import io.project.smartcontactmanager.model.User;
import io.project.smartcontactmanager.repository.ContactRepository;
import io.project.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

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
    public String processContact(@ModelAttribute Contact contact, @RequestParam("processImage") MultipartFile file, Principal principal, HttpSession session){

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

//            Success Message
            session.setAttribute("message", new Message("Your Contact is added!! Add more... ", "alert-success"));

        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

//            Error Message
            session.setAttribute("message", new Message("Something went wrong. Try  Again!!!", "alert-danger"));
        }

        return "normal/add_contact_form";
    }

//    Show Contacts Handler
//    per page = 5
//    current page = 0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal){
        m.addAttribute("title", "Show User Contacts");

//        Fetching and sending contact list to UI
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show_contacts";
    }
}
