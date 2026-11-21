package com.paisa.config;

import com.paisa.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rejects sessions issued before the owner's latest credential recovery. */
public class SessionVersionFilter extends OncePerRequestFilter {
  public static final String SESSION_VERSION = "paisa.authVersion";
  private final UserRepository users;

  public SessionVersionFilter(UserRepository users) {
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")
        && authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      HttpSession session = request.getSession(false);
      Object version = session == null ? null : session.getAttribute(SESSION_VERSION);
      boolean valid =
          version instanceof Number number
              && users
                  .findByEmail(authentication.getName())
                  .filter(
                      user ->
                          user.canAccessWorkspace() && user.getAuthVersion() == number.longValue())
                  .isPresent();
      if (!valid) {
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
