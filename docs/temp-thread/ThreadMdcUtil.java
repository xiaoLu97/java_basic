package com.sf.dipp.gateway.common.utils;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

import com.sf.dipp.gateway.constants.Constant;

/**
 * 线程MDC工具类
 *
 * @author 01391642
 * @date 2022-08-13
 */
public class ThreadMdcUtil {

    private ThreadMdcUtil() {
        throw new UnsupportedOperationException();
    }

    public static void setTraceIdIfAbsent() {
        if (MDC.get(Constant.TRACE_ID) == null) {
            MDC.put(Constant.TRACE_ID, TraceIdUtil.getTraceId());
        }
    }

    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}