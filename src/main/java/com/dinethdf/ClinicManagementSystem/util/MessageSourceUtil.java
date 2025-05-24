package com.dinethdf.ClinicManagementSystem.util;


import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceUtil {
    private final MessageSource messageSource;

    public MessageSourceUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, Object[] args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String getMessageOrCustom(String key, Object[] args, String customMessage) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return customMessage;
        }
    }
}