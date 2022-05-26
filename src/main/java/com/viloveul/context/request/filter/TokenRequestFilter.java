package com.viloveul.context.request.filter;

import com.viloveul.context.auth.dto.DetailAuthentication;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

public class TokenRequestFilter extends OncePerRequestFilter {

    private static final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    private final Environment environment;

    private final Function<String, DetailAuthentication> parser;

    public TokenRequestFilter(
        Environment environment,
        Function<String, DetailAuthentication> parser
    ) {
        this.environment = environment;
        this.parser = parser;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest req,
        @NonNull HttpServletResponse res,
        @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String header = req.getHeader(this.environment.getProperty("viloveul.security.token-header", String.class, "Authorization"));
        String query = req.getParameter(this.environment.getProperty("viloveul.security.token-query", String.class,"token"));
        String prefix = this.environment.getProperty("viloveul.security.token-prefix", String.class,"Bearer") + " ";

        if ((header == null || !header.startsWith(prefix)) && query == null) {
            chain.doFilter(req, res);
            return;
        }
        String credential = header == null ? query : header.replace(prefix, "");
        // Reads the JWT from the Authorization header, and then uses JWT to validate the token
        DetailAuthentication auth = this.parser.apply(credential);

        userDetailsChecker.check(auth);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            auth.getUsername(), credential, auth.getAuthorities()
        );
        authentication.setDetails(auth);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
}
