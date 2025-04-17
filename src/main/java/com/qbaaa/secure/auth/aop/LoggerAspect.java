package com.qbaaa.secure.auth.aop;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggerAspect {

  @Pointcut("execution(* com.qbaaa.secure.auth.controller.*.*(..))")
  public void controllerPackagePointcut() {}

  @Pointcut("execution(* com.qbaaa.secure.auth.service..*.*(..))")
  public void servicePackagePointcut() {}

  @Pointcut("execution(* com.qbaaa.secure.auth.repository.*.*(..))")
  public void repositoryPackagePointcut() {}

  @Pointcut("execution(* com.qbaaa.secure.auth.job.*.*(..))")
  public void jobMethodPointcut() {}

  @Pointcut("execution(* com.qbaaa.secure.auth.event.*.*(..))")
  public void eventMethodPointcut() {}

  @Around("jobMethodPointcut()")
  public Object logJob(ProceedingJoinPoint joinPoint) throws Throwable {
    var start = System.currentTimeMillis();
    var methodName = joinPoint.getSignature().toShortString();
    log.info("CRON JOB:");
    log.info("Start: {}", methodName);
    log.debug("Args: {}", Arrays.toString(joinPoint.getArgs()));

    Object result;
    try {
      result = joinPoint.proceed();
      return result;
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info("End: {} [{} ms]", methodName, duration);
    }
  }

  @Around(
      "controllerPackagePointcut() || servicePackagePointcut() || repositoryPackagePointcut() || eventMethodPointcut()")
  public Object logApp(ProceedingJoinPoint joinPoint) throws Throwable {
    var start = System.currentTimeMillis();
    var methodName = joinPoint.getSignature().toShortString();
    log.info("Start: {}", methodName);
    log.debug("Args: {}", Arrays.toString(joinPoint.getArgs()));

    Object result;
    try {
      result = joinPoint.proceed();
      return result;
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info("End: {} [{} ms]", methodName, duration);
    }
  }
}
