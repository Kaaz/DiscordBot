/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.core;

import java.util.Arrays;

public class Logger {
    private final static boolean LOG_PRINT_STACK_SOURCE = false;
    private final static LogLevel minLogLevel = LogLevel.DEBUG;

    public static void debug(Object... message) {
        print(LogLevel.DEBUG, message);
    }

    public static void debugf(String msg, Object... params) {
        print(LogLevel.DEBUG, String.format(msg, params));
    }

    public static void info(Object... message) {
        print(LogLevel.INFO, message);
    }

    public static void infof(String msg, Object... params) {
        print(LogLevel.INFO, String.format(msg, params));
    }

    public static void warn(Object... message) {
        print(LogLevel.WARN, message);
    }

    public static void warnf(String msg, Object... params) {
        print(LogLevel.WARN, String.format(msg, params));
    }

    public static void fatal(Object... message) {
        print(LogLevel.FATAL, message);
    }

    public static void fatalf(String msg, Object... params) {
        print(LogLevel.FATAL, String.format(msg, params));
    }

    public static void fatal(Throwable e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }

    public static void fatal(Throwable e, Object... message) {
        print(LogLevel.FATAL, message);
    }

    private static void print(LogLevel logLevel, Object... message) {
        if (logLevel.ordinal() >= minLogLevel.ordinal()) {
            System.out.println(String.format("%5s: %s", logLevel, Arrays.toString(message)));
            if (LOG_PRINT_STACK_SOURCE) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 3; i < 6 && i < stack.length; i++) {
                    System.out.println((String.format("%40s:\t\t%s", logLevel, (stack[i].toString()))));
                }
            }
        }
    }
}
