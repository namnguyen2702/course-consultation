package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.user.RegisterRequest;
import vn.coursebooking.user.UserService;

@Controller
public class RegistrationPageController {

    private final UserService userService;

    public RegistrationPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String submitRegistration(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "errorMessage",
                    bindingResult.getAllErrors()
                            .get(0)
                            .getDefaultMessage()
            );

            return "register";
        }

        try {
            userService.register(request);

        } catch (ResponseStatusException exception) {
            model.addAttribute(
                    "errorMessage",
                    exception.getReason()
            );

            return "register";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đăng ký thành công. Bạn hãy đăng nhập."
        );

        return "redirect:/login";
    }
}