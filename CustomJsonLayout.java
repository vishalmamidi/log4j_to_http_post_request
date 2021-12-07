package com.jb.application.logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.nio.charset.Charset;

@Plugin(name = "CustomJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE)
public class CustomJsonLayout extends AbstractStringLayout {
    private static final Gson gson = new Gson();

    public CustomJsonLayout(Configuration config, Charset aCharset, Serializer headerSerializer, Serializer footerSerializer) {
        super(config, aCharset, headerSerializer, footerSerializer);
    }

    @PluginFactory
    public static CustomJsonLayout createLayout(@PluginConfiguration final Configuration config,
                                                @PluginAttribute(value = "charset", defaultString = "US-ASCII") final Charset charset) {
        return new CustomJsonLayout(config, charset, null, null);
    }

    @Override
    public String toSerializable(LogEvent event) {
        JsonObject jsonObject = new JsonObject();

        // as example
        jsonObject.addProperty("application_name", "MyApp");
        jsonObject.addProperty("timestamp", "" + System.currentTimeMillis());

        // some log Information
        jsonObject.addProperty("level", event.getLevel().name());
        jsonObject.addProperty("thread", event.getThreadName());
        jsonObject.addProperty("thread_id", event.getThreadId());
        jsonObject.addProperty("logger_name", event.getLoggerName());

        // extra information
        final StackTraceElement source = event.getSource();
        JsonObject sourceObject = new JsonObject();
        sourceObject.addProperty("class", source.getClassName());
        sourceObject.addProperty("method", source.getMethodName());
        sourceObject.addProperty("file", source.getFileName());
        sourceObject.addProperty("line", source.getLineNumber());
        jsonObject.add("source", sourceObject);

        // your log message
        jsonObject.addProperty("message", event.getMessage().getFormattedMessage());

        // Exceptions
        if (event.getThrownProxy() != null) {
            final ThrowableProxy thrownProxy = event.getThrownProxy();
            final Throwable throwable = thrownProxy.getThrowable();

            final String exceptionsClass = throwable.getClass().getCanonicalName();
            if (exceptionsClass != null) {
                jsonObject.addProperty("exception", exceptionsClass);
            }

            final String exceptionsMessage = throwable.getMessage();
            if (exceptionsMessage != null) {
                jsonObject.addProperty("cause", exceptionsMessage);
            }

            final String stackTrace = thrownProxy.getExtendedStackTraceAsString("");
            if (stackTrace != null) {
                jsonObject.addProperty("stacktrace", stackTrace);
            }
        }

        return gson.toJson(jsonObject).concat("\r\n");
    }

}