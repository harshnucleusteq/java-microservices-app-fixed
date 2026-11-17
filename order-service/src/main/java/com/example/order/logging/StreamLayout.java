package com.example.order.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * One layout for a log file that holds two kinds of line: the business streams named in
 * {@code plainLoggers} (order-trace, order-audit) in the plain layout, everything else in the
 * located layout with the caller's file, line and method. Logback lets only one appender write
 * a file, and an appender has one layout, so the choice is made here, per event.
 * Configured from logback-spring.xml (locatedPattern, plainPattern, plainLoggers).
 */
public class StreamLayout extends LayoutBase<ILoggingEvent> {

    private final PatternLayout located = new PatternLayout();
    private final PatternLayout plain = new PatternLayout();
    private Set<String> plainLoggers = Set.of();

    public void setLocatedPattern(String pattern) {
        located.setPattern(pattern);
    }

    public void setPlainPattern(String pattern) {
        plain.setPattern(pattern);
    }

    public void setPlainLoggers(String names) {
        plainLoggers = Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void start() {
        // Same context as the configuration, so Spring's converters (%clr, %wEx, %applicationName) resolve.
        for (PatternLayout layout : new PatternLayout[] {located, plain}) {
            layout.setContext(getContext());
            layout.start();
        }
        super.start();
    }

    @Override
    public void stop() {
        located.stop();
        plain.stop();
        super.stop();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return (plainLoggers.contains(event.getLoggerName()) ? plain : located).doLayout(event);
    }
}
