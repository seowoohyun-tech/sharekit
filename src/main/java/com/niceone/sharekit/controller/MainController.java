package com.niceone.sharekit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/profile")
    public String memberInfoPage() {
        return "profile";
    }

    @GetMapping("/equipment-list")
    public String equipmentListPage() {
        return "equipment-list";
    }

    @GetMapping("/addEquipment")
    public String addEquipmentPage() {
        return "addEquipment";
    }

    @GetMapping("/changeStatus")
    public String changeStatusPage() {
        return "changeStatus";
    }

    @GetMapping("/rental")
    public String rentalPage() {
        return "rental";
    }

}