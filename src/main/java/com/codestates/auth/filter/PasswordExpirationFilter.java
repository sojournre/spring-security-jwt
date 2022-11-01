package com.codestates.auth.filter;

import com.codestates.auth.userdetails.MemberDetailService;
import com.codestates.member.entity.Member;
import com.codestates.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

//@Order(value = Ordered.HIGHEST_PRECEDENCE) // 추가 시, SecurityContextHolder 정보 가지고 오지 못함
@Component
public class PasswordExpirationFilter implements Filter {

    private final MemberRepository memberRepository;

    public PasswordExpirationFilter(MemberDetailService memberDetailService, MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (isUrlExcluded(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        System.out.println("PasswordExpirationFilter");

        Member member = getLoggedInMember();

        if (member != null && member.isPasswordExpired()) {
            showChangePasswordPage(response, httpRequest, member);
        } else {
            chain.doFilter(httpRequest, response);
        }
    }


    private boolean isUrlExcluded(HttpServletRequest httpRequest) {
        String url = httpRequest.getRequestURL().toString();
        String path = httpRequest.getRequestURI();

        if (url.endsWith("/change_password") || path.startsWith("/h2/")) {
            return true;
        }
        return false;
    }
    private Member getLoggedInMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null;

        if (authentication != null) {
            principal = authentication.getPrincipal();
        }

        if (principal instanceof MemberDetailService.MemberDetails) {
            MemberDetailService.MemberDetails memberDetails = (MemberDetailService.MemberDetails) principal;
            return memberDetails;
        } else {
            String username = principal.toString();
            Optional<Member> optionalMember = memberRepository.findByUid(username);
            return optionalMember.orElse(null);
        }
    }
    private void showChangePasswordPage(ServletResponse response, HttpServletRequest httpRequest, Member member) throws IOException {
        System.out.println("Member: " + member.getName() + " - Password Expired:");
        System.out.println("Last time passowrd changed: " + member.getPasswordChangedTime());
        System.out.println("Current time: " + new Date());

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String redirectURL = httpRequest.getContextPath() + "/change_password";
        httpResponse.sendRedirect(redirectURL);
    }
}
