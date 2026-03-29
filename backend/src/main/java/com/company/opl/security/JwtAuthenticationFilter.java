package com.company.opl.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                String realName = claims.get("realName", String.class);
                String departmentCode = claims.get("departmentCode", String.class);
                String departmentName = claims.get("departmentName", String.class);
                Collection<?> roleClaims = claims.get("roles", Collection.class);
                List<String> roles = new ArrayList<>();
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (roleClaims != null) {
                    for (Object role : roleClaims) {
                        String roleCode = String.valueOf(role);
                        roles.add(roleCode);
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
                    }
                }
                LoginUser loginUser = new LoginUser(userId, username, realName, departmentCode, departmentName, roles);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (JwtException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
