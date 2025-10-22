package nus.edu.u.file.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class GcsStorageConfigTest {

    @Test
    void storage_throws_when_env_not_set() {
        String val = System.getenv(GcsStorageConfig.GCP_SERVICE_ENV_NAME);
        // If environment variable is set in CI/dev, skip this assertion since storage() may try to
        // parse it.
        Assumptions.assumeTrue(
                val == null || val.isBlank(),
                "GCP env is set; skipping test that expects missing env");

        assertThrows(
                IllegalStateException.class,
                () -> {
                    try {
                        new GcsStorageConfig().storage();
                    } catch (Exception e) {
                        // wrap checked IOException as runtime for lambda
                        throw new RuntimeException(e);
                    }
                });
    }
}
