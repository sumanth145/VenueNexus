package com.venue.management.exception;

import com.venue.management.entity.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {

        model.addAttribute("error", ex.getMessage());

        // IMPORTANT: re-add form backing object
        model.addAttribute("user", new User());

        return "auth/register";
    }
}
