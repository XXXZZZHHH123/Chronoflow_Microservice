package nus.edu.u.file.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GcsPropertiesConfigTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validProperties_noViolations() {
        GcsPropertiesConfig cfg = new GcsPropertiesConfig();
        cfg.setBucket("my-bucket");
        cfg.setSignedUrlExpiryMinutes(10L);

        var violations = validator.validate(cfg);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidProperties_blankBucket_violations() {
        GcsPropertiesConfig cfg = new GcsPropertiesConfig();
        cfg.setBucket("");
        cfg.setSignedUrlExpiryMinutes(10L);

        var violations = validator.validate(cfg);
        assertEquals(1, violations.size());
    }

    @Test
    void invalidProperties_negativeExpiry_violations() {
        GcsPropertiesConfig cfg = new GcsPropertiesConfig();
        cfg.setBucket("b");
        cfg.setSignedUrlExpiryMinutes(0L);

        var violations = validator.validate(cfg);
        assertEquals(1, violations.size());
    }
}
