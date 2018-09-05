package pl.ezgolda.springproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.ezgolda.springproject.data.CarRepository;
import pl.ezgolda.springproject.data.OrderRepository;
import pl.ezgolda.springproject.data.RoleRepository;
import pl.ezgolda.springproject.data.UserRepository;
import pl.ezgolda.springproject.data.objects.Car;
import pl.ezgolda.springproject.data.objects.Order;
import pl.ezgolda.springproject.data.objects.User;
import pl.ezgolda.springproject.service.RecaptchaService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/")
public class IndexController {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CarRepository carRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecaptchaService recaptchaService;
    private final ApplicationContext context;

    public IndexController(UserRepository userRepository, RoleRepository roleRepository, CarRepository carRepository, OrderRepository orderRepository, PasswordEncoder passwordEncoder, RecaptchaService recaptchaService, ApplicationContext context) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.carRepository = carRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.recaptchaService = recaptchaService;
        this.context = context;
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("cars", carRepository.findAll());

//        User testUser = userRepository.getOne(3);
//        Order order = testUser.getOrders().get(0);
//        System.out.println("NAWOLNO: " + order.getCar().getModel());

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
            user = userRepository.save(user);

            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("redirect:/register?captchaerror");
    }


    @GetMapping("/login")
    public String login(Principal principal) {
        return principal == null ? "login" : "redirect:/";
    }

    @GetMapping("/admin_panel")
    public String adminPanel(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("orders", orderRepository.findAll());
        return "admin_panel";
    }

    @GetMapping("/user_panel")
    public String userPanel(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<List<Order>> orders = orderRepository.findAllByUser(user);
        model.addAttribute("user", user);
        if (orders.isPresent()) {
            model.addAttribute("orders", orders.get());
        } else {
            model.addAttribute("orders", Collections.emptyList());
        }
        return "user_panel";
    }

    @PostMapping(value = "/deleteOrder")
    public ModelAndView deleteOrder(@RequestParam("deleteOrder") Integer id) {
        orderRepository.deleteById(id);
        return new ModelAndView("redirect:/");
    }

    @PostMapping(value = "/selectUser")
    public ModelAndView selectUser(@RequestParam("selectUser") String selectUser,
                                   @Param("modifyUser") Optional<String> modifyUser,
                                   @Param("deleteUser") Optional<String> deleteUser) throws Exception {

        Optional<User> userByLogin = userRepository.findByUsername(selectUser);

        if (!userByLogin.isPresent()) {
            throw new Exception("No user selected!");
        }
        if (deleteUser.isPresent()) {
            userRepository.deleteUserById(userByLogin.get().getId());
        } else if (modifyUser.isPresent()) {
            ModelAndView mav = new ModelAndView("modify_user");
            mav.addObject("user", userByLogin.get());
            return mav;
        }
        return new ModelAndView("redirect:/admin_panel");
    }

    @RequestMapping(value = "/modifyUser", method = RequestMethod.POST)
    public ModelAndView modifyUser(@RequestParam("id") Integer id,
                                   @RequestParam(value = "username", required = false) Optional<String> username,
                                   @RequestParam(value = "password", required = false) Optional<String> password,
                                   @RequestParam("email") String email) {


        User user = userRepository.findById(id).get();
        user.setEmail(email);
        if (username.isPresent()) {
            user.setUsername(username.get());
        }
        if (password.isPresent() && password.get().length() > 3) {
            user.setPassword(passwordEncoder.encode(password.get()));
        }
        userRepository.save(user);

        return new ModelAndView("redirect:/");
    }

    @GetMapping("order/{id}")
    public String order(Model model, @PathVariable("id") Car car) {
        List<String> imageUrls = new ArrayList<>();
        try {
            Resource[] resources = context.getResources("classpath:/static/images/" + car.getModel() + "/**");
            imageUrls = Arrays.stream(resources)
                    .map(resource -> "/images/" + car.getModel() + "/" + resource.getFilename()).collect(Collectors.toList());
        } catch (IOException e) {
            model.addAttribute("error", "No images found!");
            log.error("Error while getting car image resources.", e);
        }
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("car", car);
        Optional<List<Order>> alreadyOrdered = orderRepository.findAllByCar_Id(car.getId());
//        model.addAttribute("alreadyOrdered", alreadyOrdered.isPresent() ? alreadyOrdered.get() : Collections.emptyList());
        model.addAttribute("alreadyOrdered", alreadyOrdered.orElseGet(Collections::emptyList));
        return "order";
    }

    @PostMapping("/orderCar")
    public ModelAndView orderCar(@RequestParam("startDate") LocalDate startDate,
                                 @RequestParam("endDate") LocalDate endDate,
                                 @RequestParam("carId") Integer carId,
                                 Authentication authentication, Model model, HttpServletResponse response) throws IOException {

        Optional<List<Order>> list = orderRepository.findAllByCar_Id(carId);
        if (list.isPresent()) {
            for (Order order : list.get()
            ) {
                if ((order.getRentStartDate().isBefore(endDate) && order.getRentEndDate().isBefore(startDate)) ||
                        (order.getRentStartDate().isAfter(endDate) && order.getRentEndDate().isAfter(startDate))) {
                    System.out.println("jest ok");
                } else {
                    System.out.println("NIEEEE");
                    return new ModelAndView("redirect:/order/" + carId + "?error");
                }

            }
        }

        if (startDate.isAfter(endDate) || startDate.isBefore(new LocalDate(LocalDate.now()))) {
            System.out.println("NIEEEE");
            return new ModelAndView("redirect:/order/" + carId + "?error");
        }

        User user = (User) authentication.getPrincipal();
        Order orderCar = new Order(user, carRepository.getOne(carId), startDate, endDate);
        orderRepository.save(orderCar);

        return new ModelAndView("redirect:/order/" + carId + "?success");

    }

    @PostMapping("/downloadPDF")
    public void downloadPDF(@RequestParam("startDate") LocalDate startDate,
                            @RequestParam("endDate") LocalDate endDate,
                            @RequestParam("carId") Integer carId,
                            Authentication authentication, HttpServletResponse response) {

        User user = (User) authentication.getPrincipal();
        Order orderCar = new Order(user, carRepository.getOne(carId), startDate, endDate);
        Integer orderCost = (endDate.getDayOfYear() - startDate.getDayOfYear() + 1) * carRepository.getOne(carId).getPrice();
        System.out.println("koszt wypozyczki " + orderCost);
        try (ByteArrayOutputStream output = createPDF(orderCar, orderCost);
             ServletOutputStream servletOutputStream = response.getOutputStream()) {
            servletOutputStream.write(output.toByteArray());
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=\"confirmation.pdf\"");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /////////////////////////////////////////////////////////////////////


    //    @ExceptionHandler
//    public String exceptionHandler() {
//        return "error";
//    }
    public ByteArrayOutputStream createPDF(Order order, Integer orderCost) throws IOException {


        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("POTWIERDZENIE ZAMOWIENIA / ORDER CONFIRMATION");
        contentStream.newLine();
        contentStream.newLine();
        contentStream.showText("Model: " + order.getCar().getModel());
        contentStream.newLine();
        contentStream.showText("Od / From: " + order.getRentStartDate().toString());
        contentStream.newLine();
        contentStream.showText("Do / To: " + order.getRentEndDate().toString());
        contentStream.newLine();
        contentStream.showText("Calkowity koszt / Total cost: " + orderCost.toString());
        contentStream.endText();
        contentStream.fillAndStrokeEvenOdd();
        contentStream.close();

        document.save(output);
        document.close();

        return output;
    }
}
