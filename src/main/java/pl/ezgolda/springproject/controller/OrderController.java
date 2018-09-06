package pl.ezgolda.springproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import pl.ezgolda.springproject.data.CarRepository;
import pl.ezgolda.springproject.data.OrderRepository;
import pl.ezgolda.springproject.data.objects.Car;
import pl.ezgolda.springproject.data.objects.Order;
import pl.ezgolda.springproject.data.objects.User;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final ApplicationContext context;
    private final CarRepository carRepository;

    public OrderController(OrderRepository orderRepository, ApplicationContext context, CarRepository carRepository) {
        this.orderRepository = orderRepository;
        this.context = context;
        this.carRepository = carRepository;
    }

    //region ORDER
    @PostMapping(value = "/deleteOrder")
    public ModelAndView deleteOrder(@RequestParam("deleteOrder") Integer id) {
        orderRepository.deleteById(id);
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
                                 Authentication authentication) {

        Optional<List<Order>> list = orderRepository.findAllByCar_Id(carId);
        if (list.isPresent()) {
            for (Order order : list.get()
            ) {
                if ((order.getRentStartDate().isBefore(endDate) && order.getRentEndDate().isBefore(startDate)) ||
                        (order.getRentStartDate().isAfter(endDate) && order.getRentEndDate().isAfter(startDate))) {
                } else {
                    return new ModelAndView("redirect:/order/" + carId + "?error");
                }

            }
        }

        if (startDate.isAfter(endDate) || startDate.isBefore(new LocalDate(LocalDate.now()))) {
            return new ModelAndView("redirect:/order/" + carId + "?error");
        }

        User user = (User) authentication.getPrincipal();
        Order orderCar = new Order(user, carRepository.getOne(carId), startDate, endDate);
        orderRepository.save(orderCar);

        return new ModelAndView("redirect:/order/" + carId + "?success");

    }
    //endregion
}
