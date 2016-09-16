package com.itextpdf.rups.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHelper {

    public static void warn(String message, Exception e, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        logger.warn(message);
        logger.debug(message, e);
    }

    public static void warn(String message, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        logger.warn(message);
        logger.debug(message);
    }

    public static void error(String message, Exception e, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        logger.error(message);
        logger.debug(message, e);
    }

    public static void error(String message, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        logger.error(message);
        logger.debug(message);
    }

    public static void info(String message, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        logger.info(message);
        logger.debug(message);
    }

    public static void warn(String message, Exception e, Class c) {
        warn(message, e, c.getName());
    }

    public static void warn(String message, Class c) {
        warn(message, c.getName());
    }

    public static void error(String message, Exception e, Class c) {
        error(message, e, c.getName());
    }

    public static void error(String message, Class c) {
        error(message, c.getName());
    }

    public static void info(String message, Class c) {
        info(message, c.getName());
    }
}
