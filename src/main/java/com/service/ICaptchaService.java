package com.service;


public interface ICaptchaService {
    
    default void processResponse(final String response) throws Exception {}
    
    default void processResponse(final String response, String action) throws Exception {}
    
    String getReCaptchaSite();

    String getReCaptchaSecret();
}
