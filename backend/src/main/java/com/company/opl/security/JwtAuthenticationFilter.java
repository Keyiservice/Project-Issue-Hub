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
    private final com.company.opl.mapper.SysUserMapper sysUserMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   com.company.opl.mapper.SysUserMapper sysUserMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sysUserMapper = sysUserMapper;
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

                if (userId != null) {
                    com.company.opl.entity.SysUser user = sysUserMapper.selectById(userId);
                    if (user != null && user.getPasswordChangeRequired() != null && user.getPasswordChangeRequired() == 1) {
                        String path = request.getRequestURI();
                        if (!path.startsWith("/api/auth/change-password")
                                && !path.startsWith("/api/auth/mfa/")
                                && !path.startsWith("/api/auth/me")) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }
                    }
                }
            } catch (JwtException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
