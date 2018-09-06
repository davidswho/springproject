package pl.ezgolda.springproject.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.joda.time.LocalDate;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.ezgolda.springproject.data.CarRepository;
import pl.ezgolda.springproject.data.OrderRepository;
import pl.ezgolda.springproject.data.UserRepository;
import pl.ezgolda.springproject.data.objects.Order;
import pl.ezgolda.springproject.data.objects.User;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarRepository carRepository;

    public UserController(UserRepository userRepository, OrderRepository orderRepository,
                          PasswordEncoder passwordEncoder, CarRepository carRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.carRepository = carRepository;
    }

    //region USER
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

    @PostMapping("/downloadPDF")
    public void downloadPDF(@RequestParam("startDate") LocalDate startDate,
                            @RequestParam("endDate") LocalDate endDate,
                            @RequestParam("carId") Integer carId,
                            Authentication authentication, HttpServletResponse response) {

        User user = (User) authentication.getPrincipal();
        Order orderCar = new Order(user, carRepository.getOne(carId), startDate, endDate);
        Integer orderCost = (endDate.getDayOfYear() - startDate.getDayOfYear() + 1) * carRepository.getOne(carId).getPrice();
        try (ByteArrayOutputStream output = createPDF(orderCar, orderCost);
             ServletOutputStream servletOutputStream = response.getOutputStream()) {
            servletOutputStream.write(output.toByteArray());
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=\"confirmation.pdf\"");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private ByteArrayOutputStream createPDF(Order order, Integer orderCost) throws IOException {


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
    //endregion
}
