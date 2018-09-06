package pl.ezgolda.springproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import pl.ezgolda.springproject.data.CarRepository;
import pl.ezgolda.springproject.data.RoleRepository;
import pl.ezgolda.springproject.data.UserRepository;
import pl.ezgolda.springproject.data.objects.User;
import pl.ezgolda.springproject.service.RecaptchaService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/")
public class IndexController {


    private final RoleRepository roleRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecaptchaService recaptchaService;

    public IndexController(UserRepository userRepository, RoleRepository roleRepository,
                           CarRepository carRepository,
                           PasswordEncoder passwordEncoder, RecaptchaService recaptchaService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
        this.recaptchaService = recaptchaService;
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("cars", carRepository.findAll());
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/registerUser")
    public ModelAndView addUser(@RequestParam("username") String username,
                                @RequestParam("email") String email,
                                @RequestParam("password") String password,
                                @RequestParam("repeatpassword") String repeatpassword,
                                @RequestParam(name = "g-recaptcha-response") String recaptchaResponse,
                                HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String captchaVerifyMsg = recaptchaService.verifyRecaptcha(ip, recaptchaResponse);

        if (!StringUtils.isNotEmpty(captchaVerifyMsg)) {
            User user = new User(username, passwordEncoder.encode(password), email);
            Optional<User> userByName = userRepository.findByUsername(username);
            if (userByName.isPresent()) {
                return new ModelAndView("redirect:/register?usererror");
            }
            if (!password.equals(repeatpassword)) {
                return new ModelAndView("redirect:/register?passerror");
            }
            user.addRole(roleRepository.findByName("ROLE_USER").get());
            userRepository.save(user);

            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("redirect:/register?captchaerror");
    }


    @GetMapping("/login")
    public String login(Principal principal) {
        return principal == null ? "login" : "redirect:/";
    }

}
