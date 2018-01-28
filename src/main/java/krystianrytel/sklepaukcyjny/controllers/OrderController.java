package krystianrytel.sklepaukcyjny.controllers;


import krystianrytel.sklepaukcyjny.models.Cart;
import krystianrytel.sklepaukcyjny.models.Item;
import krystianrytel.sklepaukcyjny.models.TheOrder;
import krystianrytel.sklepaukcyjny.models.User;
import krystianrytel.sklepaukcyjny.services.CartService;
import krystianrytel.sklepaukcyjny.services.ItemService;
import krystianrytel.sklepaukcyjny.services.OrderService;
import krystianrytel.sklepaukcyjny.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping("/add")
    public String saveOrder(HttpSession session, RedirectAttributes ra){
        String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUser(currentPrincipalName);
        List<Cart> userCarts = cartService.getUserCarts(currentPrincipalName);
        if( cartService.getCartsPrice(currentPrincipalName) <= user.getWallet() ){
            user.setWallet(user.getWallet()-cartService.getCartsPrice(currentPrincipalName));
            orderService.saveUserOrder(userCarts);
            ra.addFlashAttribute("message", "<div class='green-text'>Zamówiono!</div>");
        }else{
            ra.addFlashAttribute("message", "<div class='red-text'>Nie masz wystarczająco dużo środków!</div>");
        }
        ra.addFlashAttribute("userOrders", orderService.getUserOrders(currentPrincipalName));
        ra.addFlashAttribute("ordersPrice", orderService.getOrdersPrice(orderService.getUserOrders(currentPrincipalName)));
        session.setAttribute("cartlist", cartService.getUserCarts(currentPrincipalName));
        return "redirect:/order/list";
    }


    @GetMapping("/list")
    public String showOrdersList(Model model,  HttpSession session) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<TheOrder> userOrders = orderService.getUserOrders(username);
        model.addAttribute("userOrders", userOrders);
        model.addAttribute("itemsPrice", orderService.getOrdersPrice(userOrders));
        session.setAttribute("user", userService.getUser(username));
        return "/order/list";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder2) {//Rejestrujemy edytory właściwości
        DecimalFormat numberFormat = new DecimalFormat("#0.00");
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setGroupingUsed(false);
        CustomNumberEditor priceEditor = new CustomNumberEditor(Float.class, numberFormat, true);
        binder2.registerCustomEditor(Float.class, "ordersPrice", priceEditor);
        binder2.registerCustomEditor(Float.class, "cart.item.price", priceEditor);
    }
}