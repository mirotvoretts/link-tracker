package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class LinkChangeDetailsTest {

    @Test
    void testTruncatePreviewTo200Characters() {
        String longText = "a".repeat(300);

        String truncated = LinkChangeDetails.truncate(longText);

        assertTrue(truncated.length() <= 203);
        assertTrue(truncated.endsWith("..."));
    }

    @Test
    void testTruncateWithExactly200Characters() {
        String text = "a".repeat(200);

        String truncated = LinkChangeDetails.truncate(text);

        assertEquals(text, truncated);
        assertFalse(truncated.endsWith("..."));
    }

    @Test
    void testTruncateShortText() {
        String text = "Short text";

        String truncated = LinkChangeDetails.truncate(text);

        assertEquals(text, truncated);
    }

    @Test
    void testTruncateEmptyString() {
        String text = "";

        String truncated = LinkChangeDetails.truncate(text);

        assertEquals("", truncated);
    }

    @Test
    void testTruncateNullString() {
        String text = null;

        String truncated = LinkChangeDetails.truncate(text);

        assertEquals("", truncated);
    }
}
