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
        String defaultMessage = formatKeyAsDefault(key);

        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, Object[] args) {
        String defaultMessage = formatKeyAsDefault(key);
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    public String getMessageCustom(String customMessage) {
        return customMessage;
    }

    public String getMessageOrCustom(String key, Object[] args, String customMessage) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return customMessage;
        }
    }
    private String formatKeyAsDefault(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "Invalid message key";
        }

        // Replace dots with spaces
        String formatted = key.replace('.', ' ');

        // Capitalize the first letter
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}