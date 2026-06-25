package io.cinema.msnotifications.util;

import com.samskivert.mustache.Mustache;
import io.cinema.domain.exceptions.CinemaException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.cinema.domain.enumerated.CinemaExceptionTypes.TECHNICAL_ERROR;

@Component
@RequiredArgsConstructor
public class TemplateEngine {
    private static final String TEMPLATES_BASE_PATH = "classpath:templates/";
    private final ResourceLoader resourceLoader;
    private final Mustache.Compiler mustacheCompiler;

    public String buildTemplate(
            String templateName,
            Map<String, Object> variables
    ) {
        var cleanName = templateName.startsWith("/") ? templateName.substring(1) : templateName;
        var fullPath = TEMPLATES_BASE_PATH + cleanName;

        Resource resource = resourceLoader.getResource(fullPath);

        if (!resource.exists()) {
            throw new CinemaException("Template file not found at path: " + fullPath, TECHNICAL_ERROR);
        }

        StringWriter writer = new StringWriter();

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            var template = mustacheCompiler.compile(reader);
            template.execute(variables, writer);

            return writer.toString();
        } catch (IOException e) {
            throw new CinemaException(
                    "Error reading template: " + templateName + " Error: " + e.getMessage(),
                    TECHNICAL_ERROR
            );
        }
    }
}
