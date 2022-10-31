package com.codestates.auth.handler;

import com.codestates.auth.userdetails.MemberDetailService;
import com.codestates.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    public MemberAuthenticationSuccessHandler(@Lazy MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("# Authenticated successfully!");

        MemberDetailService.MemberDetails memberDetails = (MemberDetailService.MemberDetails) authentication.getPrincipal();
        if (memberDetails.getFailedAttempt() > 0) {
            memberService.resetFailedAttempts(memberDetails.getUid());
        }
    }
}
