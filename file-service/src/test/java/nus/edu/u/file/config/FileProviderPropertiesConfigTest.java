package nus.edu.u.file.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FileProviderPropertiesConfigTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validProvider_noViolations() {
        FileProviderPropertiesConfig cfg = new FileProviderPropertiesConfig();
        cfg.setProvider("gcs");

        var violations = validator.validate(cfg);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidProvider_blank_violations() {
        FileProviderPropertiesConfig cfg = new FileProviderPropertiesConfig();
        cfg.setProvider("");

        var violations = validator.validate(cfg);
        assertEquals(1, violations.size());
    }
}
