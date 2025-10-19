package nus.edu.u.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateRendererTest {

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new TemplateRenderer();
    }

    @Test
    void render_replacesPlaceholdersWithValues() {
        String template = "Hello {{name}}, your code is {{code}}.";
        String rendered = renderer.render(template, Map.of("name", "Alice", "code", 123456));

        assertThat(rendered).isEqualTo("Hello Alice, your code is 123456.");
    }

    @Test
    void render_leavesUnknownPlaceholderUntouched() {
        String template = "Hi {{name}}, location {{city}}.";

        String rendered = renderer.render(template, Map.of("name", "Bob"));

        assertThat(rendered).isEqualTo("Hi Bob, location {{city}}.");
    }

    @Test
    void render_returnsTemplateWhenVariablesEmpty() {
        String template = "Plain text";
        assertThat(renderer.render(template, Map.of())).isEqualTo(template);
        assertThat(renderer.render(template, null)).isEqualTo(template);
    }

    @Test
    void extractPlaceholders_returnsUniqueKeysInOrder() {
        String template = "{{user}} invited {{user}} to {{event}}";

        Set<String> placeholders = renderer.extractPlaceholders(template);

        assertThat(placeholders).containsExactly("user", "event");
    }

    @Test
    void extractPlaceholders_returnsEmptySetWhenNone() {
        assertThat(renderer.extractPlaceholders(null)).isEmpty();
        assertThat(renderer.extractPlaceholders("no placeholders here")).isEmpty();
    }
}
