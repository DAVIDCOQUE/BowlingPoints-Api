package com.bowlingpoints.config.jwt;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String token = getTokenFromRequest(request);
        if(token == null){
            filterChain.doFilter(request,response);
            return;
        }
        filterChain.doFilter(request,response);
    }





    private String getTokenFromRequest(HttpServletRequest request){
       final String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);

       if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
           return authHeader.substring(7);
       }
       return null;
    }


}
