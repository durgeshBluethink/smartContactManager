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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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
                contact.setImage("contact.png");
            }else{
//                copy the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img/contactImage").getFile();
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
        m.addAttribute("totalPage", contacts.getTotalPages());
        return "normal/show_contacts";
    }

    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal){
        System.out.println("cId " + cId);
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if(user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
        }
        return "normal/contact_details";
    }

//    Delete Contact Handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession httpSession){
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

//      Contact contact = this.contactRepository.findById(cId).get();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if(user.getId() == contact.getUser().getId()) {

//             Contact Image Deletion Task
//            contact.setUser(null);

            user.getContacts().remove(contact);
            this.userRepository.save(user);

           // this.contactRepository.delete(contact);
            httpSession.setAttribute("message", new Message("Contact Deleted Successfully...", "alert-success"));
        }

        return "redirect:/user/show-contacts/0";
    }


//    Update Form handler

    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model m){
        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cid).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

//    Update Contact Handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile multipartFile, Model m, HttpSession session, Principal principal){

        try{
//            Old Contact Details
            Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
            if(!multipartFile.isEmpty()){

//              Delete photo from envir
                File deleteFile = new ClassPathResource("static/img/contactImage").getFile();
                File file1 = new File(deleteFile, oldcontactDetail.getImage());
                file1.delete();

//                Update New photo
                File saveFile = new ClassPathResource("static/img/contactImage").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(multipartFile.getOriginalFilename());
            }
            else{
                contact.setImage(oldcontactDetail.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your Contact is updated...", "alert-success"));

        }
        catch(Exception e){
            e.printStackTrace();;
        }


        System.out.println("Contact Name " + contact.getName());
        return "redirect:/user/" + contact.getcId() + "/contact";
    }

//    Profile Handler
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

//    Open Settings Handler
    @GetMapping("/settings")
    public String openSettings(){
        return "normal/settings";
    }

// Changing Password Handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession httpSession){

        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);

        if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())){
//            change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            httpSession.setAttribute("message", new Message("Your Password is successfully changed!", "alert-success"));
        }
        else{
//            throw the error
            httpSession.setAttribute("message", new Message("Please Enter correct old password !!", "alert-error"));
            return "redirect:/user/settings";
        }

        return "redirect:/user/index";
    }
}
