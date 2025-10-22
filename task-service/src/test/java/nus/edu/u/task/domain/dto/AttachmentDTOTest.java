package nus.edu.u.task.domain.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AttachmentDTOTest {

    @Test
    void record_fields_roundtrip() {
        byte[] bytes = new byte[] {1, 2, 3};
        AttachmentDTO a = new AttachmentDTO("f.png", "image/png", bytes, null, true, "cid");

        assertEquals("f.png", a.filename());
        assertEquals("image/png", a.contentType());
        assertArrayEquals(bytes, a.bytes());
        assertEquals(true, a.inline());
        assertEquals("cid", a.contentId());
    }
}
