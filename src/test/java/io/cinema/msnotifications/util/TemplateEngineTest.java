package io.cinema.msnotifications.util;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.cinema.domain.exceptions.CinemaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateEngineTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Mustache.Compiler mustacheCompiler;

    @Mock
    private Resource resource;

    @Mock
    private Template template;

    @InjectMocks
    private TemplateEngine templateEngine;

    @Test
    void shouldBuildTemplateSuccessfully() throws IOException {
        // arrange
        var templateName = "email.html";
        var expectedPath = "classpath:templates/email.html";
        var variables = Map.<String, Object>of("name", "John Doe");
        var expectedOutput = "Hello John Doe!";

        when(resourceLoader.getResource(expectedPath)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Hello {{name}}!".getBytes()));
        when(mustacheCompiler.compile(any(InputStreamReader.class))).thenReturn(template);

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write(expectedOutput);
            return null;
        }).when(template).execute(eq(variables), any(Writer.class));

        // act
        String result = templateEngine.buildTemplate(templateName, variables);

        // assert
        assertThat(result).isEqualTo(expectedOutput);
        verify(resourceLoader).getResource(expectedPath);
    }

    @Test
    void shouldCleanLeadingSlashFromTemplateName() throws IOException {
        // arrange
        var templateNameWithSlash = "/email.html";
        var expectedPath = "classpath:templates/email.html";
        var variables = Map.<String, Object>of();

        when(resourceLoader.getResource(expectedPath)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mustacheCompiler.compile(any(InputStreamReader.class))).thenReturn(template);

        // act
        templateEngine.buildTemplate(templateNameWithSlash, variables);

        // assert
        verify(resourceLoader).getResource(expectedPath);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTemplateNotFound() {
        // arrange
        var templateName = "missing.html";
        var expectedPath = "classpath:templates/missing.html";

        when(resourceLoader.getResource(expectedPath)).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        // act & assert
        var variables = Map.<String, Object>of();
        assertThatThrownBy(() -> templateEngine.buildTemplate(templateName, variables))
                .isInstanceOf(CinemaException.class)
                .hasMessage("Template file not found at path: " + expectedPath);
    }

    @Test
    void shouldThrowCinemaExceptionOnIoError() throws IOException {
        // arrange
        var templateName = "corrupt.html";
        var expectedPath = "classpath:templates/corrupt.html";

        when(resourceLoader.getResource(expectedPath)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(new IOException("Disk read error"));

        // act & assert
        var variables = Map.<String, Object>of();
        assertThatThrownBy(() -> templateEngine.buildTemplate(templateName, variables))
                .isInstanceOf(CinemaException.class)
                .hasMessageContaining("Error reading template: corrupt.html")
                .hasMessageContaining("Disk read error");
    }
}