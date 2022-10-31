package com.codestates.auth.handler;

import com.codestates.auth.dto.LoginDto;
import com.codestates.member.entity.Member;
import com.codestates.member.service.MemberService;
import com.codestates.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final MemberService memberService;

    public MemberAuthenticationFailureHandler(@Lazy MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        Member member = memberService.findMemberByUid(loginDto.getUsername());

        if (member != null) {
            if (member.getMemberStatus() == Member.MemberStatus.MEMBER_ACTIVE && member.isIsAccountNonLocked()) {
                if (member.getFailedAttempt() < MemberService.MAX_FAILED_ATTEMPTS - 1) {
                    memberService.increaseFailedAttempts(member);
                } else {
                    memberService.lock(member);
                    exception = new LockedException("Your account has been locked due to 5 failed attempts."
                            + " It will be unlocked after 24 hours.");
                }
            } else if (!member.isIsAccountNonLocked()) {
                if (memberService.unlockWhenTimeExpired(member)) {
                    exception = new LockedException("Your account has been unlocked. Please try to login again");
                }
            }
        }

        log.error("# Authentication failed: {}", exception.getMessage());

        sendErrorResponse(response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
