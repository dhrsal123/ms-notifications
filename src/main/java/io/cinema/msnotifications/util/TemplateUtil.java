package io.cinema.msnotifications.util;

import com.samskivert.mustache.Mustache;
import io.cinema.domain.exceptions.CinemaException;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static io.cinema.domain.enumerated.CinemaExceptionTypes.TECHNICAL_ERROR;

@UtilityClass
public class TemplateUtil {

    private static final String TEMPLATES_PATH = "templates/";

    public static String buildTemplate(
            String templateName,
            Map<String, Object> variables
    ) {
        InputStream is = TemplateUtil.class.getClassLoader().getResourceAsStream(TEMPLATES_PATH + templateName);

        if (Objects.isNull(is)) {
            throw new IllegalArgumentException("Template file not found: " + templateName);
        }

        StringWriter writer = new StringWriter();

        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Mustache.compiler()
                    .compile(reader)
                    .execute(variables, writer);
        } catch (IOException e) {
            throw new CinemaException(
                    "Error reading template: " + templateName + " Error: " + e.getMessage(), TECHNICAL_ERROR
            );
        }

        return writer.toString();
    }
}
