package com.coursegen.coursegen_backend.Util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class JwtAuthenticationFilter extends  OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/auth/login",
            "/auth/signup"
    );

    private boolean isPublicUrl(HttpServletRequest request) {
        String path = request.getServletPath();
        System.out.println(path);
        for (String url : EXCLUDE_URLS) {
            if (path.startsWith(url)) {
                System.out.println("Yes");
                return true;
            }
        }
        return false;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
System.out.println("reachsecurity1");
        String authHeader = request.getHeader("Authorization");
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                System.out.println(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println(authentication.getPrincipal());
            }
        }


        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT check for these public endpoints
        return isPublicUrl(request);
    }
}
