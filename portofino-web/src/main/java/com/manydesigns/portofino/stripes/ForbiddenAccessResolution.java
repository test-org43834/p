/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.shiro.ShiroUtils;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ForbiddenAccessResolution implements Resolution {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ForbiddenAccessResolution.class);

    public static final int UNAUTHORIZED = 403;

    private String errorMessage;

    public ForbiddenAccessResolution() {}

    public ForbiddenAccessResolution(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        String userId = null;
        if (subject.isAuthenticated()) {
            userId = subject.getPrincipal().toString();
        }
        String originalPath = ServletUtils.getOriginalPath(request);
        UrlBuilder urlBuilder =
                new UrlBuilder(Locale.getDefault(), originalPath, false);
        Map parameters = request.getParameterMap();
        urlBuilder.addParameters(parameters);
        String returnUrl = urlBuilder.toString();
        boolean ajax = "true".equals(request.getParameter("ajax"));
        if (userId == null && !ajax) {
            logger.info("Anonymous user not allowed. Redirecting to login.");
            Application application = (Application) request.getAttribute(RequestAttributes.APPLICATION);
            String loginLink = ShiroUtils.getLoginLink(application, request.getContextPath(), returnUrl, "/");
            new RedirectResolution(loginLink, false).execute(request, response);
        } else {
            if(ajax) {
                logger.debug("AJAX call while user disconnected");
                Application application = (Application) request.getAttribute(RequestAttributes.APPLICATION);
                //TODO where to redirect?
                String loginLink = ShiroUtils.getLoginLink(application, request.getContextPath(), "/", "/");
                response.setStatus(UNAUTHORIZED);
                new StreamingResolution("text/plain", loginLink).execute(request, response);
            } else {
                logger.warn("User {} not authorized for url {}.", userId, returnUrl);
                new ErrorResolution(UNAUTHORIZED, errorMessage).execute(request, response);
            }
        }
    }
}
