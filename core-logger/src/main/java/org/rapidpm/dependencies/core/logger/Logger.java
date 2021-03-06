package org.rapidpm.dependencies.core.logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.rapidpm.dependencies.core.logger.factory.LoggerFactory;
import org.rapidpm.dependencies.core.logger.factory.NoLogFactory;
import org.rapidpm.dependencies.core.logger.factory.StandardLoggerFactory;

/**
 *
 */
public class Logger {

  public static final String RAPIDPM_LOGGING_TYPE = "rapidpm.logging.type";
  public static final String RAPIDPM_LOGGING_CLASS = "rapidpm.logging.class";

  private static volatile LoggerFactory loggerFactory;
  private static final Object FACTORY_LOCK = new Object();

  private Logger() {
  }

  public static LoggingService getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  public static LoggingService getLogger(String name) {
    //noinspection DoubleCheckedLocking
    if (loggerFactory == null) {
      //noinspection SynchronizationOnStaticField
      synchronized (FACTORY_LOCK) {
        if (loggerFactory == null) {
          String loggerType = System.getProperty(RAPIDPM_LOGGING_TYPE);
          loggerFactory = newLoggerFactory(loggerType);
        }
      }
    }
    return loggerFactory.getLogger(name);
  }

  public static LoggerFactory newLoggerFactory(String loggerType) {
    LoggerFactory loggerFactory = null;
    String loggerClass = System.getProperty(RAPIDPM_LOGGING_CLASS);
    if (loggerClass != null) {
      loggerFactory = loadLoggerFactory(loggerClass);
    }

    if (loggerFactory == null) {
      if (loggerType != null) {
        if ("log4j".equals(loggerType)) {
          loggerFactory = loadLoggerFactory("org.rapidpm.dependencies.core.logger.factory.Log4jFactory");
        } else if ("log4j2".equals(loggerType)) {
          loggerFactory = loadLoggerFactory("org.rapidpm.dependencies.core.logger.factory.Log4j2Factory");
        } else if ("slf4j".equals(loggerType)) {
          loggerFactory = loadLoggerFactory("org.rapidpm.dependencies.core.logger.factory.Slf4jFactory");
        } else if ("jdk".equals(loggerType)) {
          loggerFactory = new StandardLoggerFactory();
        } else if ("none".equals(loggerType)) {
          loggerFactory = new NoLogFactory();
        }
      }
    }

    if (loggerFactory == null) {
      loggerFactory = new StandardLoggerFactory();
    }
    return loggerFactory;
  }

  private static LoggerFactory loadLoggerFactory(String className) {
    try {
      final Class<?> forName = Class.forName(className);
      final Constructor<?> declaredConstructor = forName.getDeclaredConstructor();
      return (LoggerFactory) declaredConstructor.newInstance();
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }
}
