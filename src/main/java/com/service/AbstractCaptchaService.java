package com.service;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;


public abstract class AbstractCaptchaService implements ICaptchaService{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractCaptchaService.class);
    
    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected CaptchaSettings captchaSettings;

    @Autowired
    protected ReCaptchaAttemptService reCaptchaAttemptService;

    @Autowired
    protected RestOperations restTemplate;

    protected static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
    
    protected static final String RECAPTCHA_URL_TEMPLATE = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s";

    @Override
    public String getReCaptchaSite() {
        return captchaSettings.getSiteV3();
    }

    @Override
    public String getReCaptchaSecret() {
        return captchaSettings.getSecretV3();
    }
  

    protected void securityCheck(final String response) throws Exception {
        LOGGER.debug("Attempting to validate response {}", response);

        if (reCaptchaAttemptService.isBlocked(getClientIP())) {
            throw new Exception("Client exceeded maximum number of failed attempts");
        }

        if (!responseSanityCheck(response)) {
            throw new Exception("Response contains invalid characters");
        }
    }

    protected boolean responseSanityCheck(final String response) {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
    }

    protected String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
