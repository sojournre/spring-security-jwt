package com.codestates.member.controller;

import com.codestates.auth.userdetails.MemberDetailService;
import com.codestates.member.entity.Member;
import com.codestates.member.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class PasswordController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(MemberService memberService, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/change_password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("pageTitle", "Change Expired Password");
        return "change_password";
    }

    @PostMapping("/change_password")
    public String processChangePassword(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Model model, RedirectAttributes ra,
                                        @AuthenticationPrincipal Authentication authentication) throws ServletException {
        MemberDetailService.MemberDetails memberDetails = (MemberDetailService.MemberDetails) authentication.getPrincipal();
        Member member = memberDetails;

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        model.addAttribute("pageTitle", "Change Expired Password");

        if (oldPassword.equals(newPassword)) {
            model.addAttribute("message", "Your new password must be different than the old one.");

            return "change_password";
        }
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            model.addAttribute("message", "Your old password is incorrect.");
            return "change_password";
        } else {
            memberService.changePassword(member, newPassword);
            request.logout();
            ra.addFlashAttribute("message", "You have changed your password successfully. "
            + "Please login again.");

            return "redirect:/login";
        }
    }
}
