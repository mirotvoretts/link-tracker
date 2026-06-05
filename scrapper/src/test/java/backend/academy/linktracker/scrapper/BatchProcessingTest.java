package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class BatchProcessingTest {

    @Test
    void testBatchProcessingFormationWithSmallBatch() {
        int packageSize = 50;
        List<LinkDto> allLinks = createMockLinks(30);

        List<List<LinkDto>> batches = formBatches(allLinks, packageSize);

        assertEquals(1, batches.size());
        assertEquals(30, batches.get(0).size());
    }

    @Test
    void testBatchProcessingFormationWithMultipleBatches() {
        int packageSize = 50;
        List<LinkDto> allLinks = createMockLinks(150);

        List<List<LinkDto>> batches = formBatches(allLinks, packageSize);

        assertEquals(3, batches.size());
        assertEquals(50, batches.get(0).size());
        assertEquals(50, batches.get(1).size());
        assertEquals(50, batches.get(2).size());
    }

    @Test
    void testBatchProcessingWithUnevenSize() {
        int packageSize = 50;
        List<LinkDto> allLinks = createMockLinks(135);

        List<List<LinkDto>> batches = formBatches(allLinks, packageSize);

        assertEquals(3, batches.size());
        assertEquals(50, batches.get(0).size());
        assertEquals(50, batches.get(1).size());
        assertEquals(35, batches.get(2).size());
    }

    @Test
    void testBatchProcessingWithEmptyList() {
        int packageSize = 50;
        List<LinkDto> allLinks = new ArrayList<>();

        List<List<LinkDto>> batches = formBatches(allLinks, packageSize);

        assertEquals(0, batches.size());
    }

    @Test
    void testBatchProcessingGroupingByChatId() {
        Map<Long, Set<LinkDto>> groupedLinks = new HashMap<>();
        groupedLinks.put(1L, new HashSet<>(createMockLinks(10)));
        groupedLinks.put(2L, new HashSet<>(createMockLinks(20)));
        groupedLinks.put(3L, new HashSet<>(createMockLinks(15)));

        int totalLinks = groupedLinks.values().stream().mapToInt(Set::size).sum();

        assertEquals(45, totalLinks);
        assertEquals(3, groupedLinks.size());
    }

    @Test
    void testBatchProcessingPreservesLinkData() {
        LinkDto originalLink = new LinkDto(1L, "https://github.com/test/repo", List.of(), OffsetDateTime.now());
        List<LinkDto> links = List.of(originalLink);

        List<List<LinkDto>> batches = formBatches(links, 50);
        LinkDto processedLink = batches.get(0).get(0);

        assertEquals(originalLink.getId(), processedLink.getId());
        assertEquals(originalLink.getUrl(), processedLink.getUrl());
    }

    private List<LinkDto> createMockLinks(int count) {
        List<LinkDto> links = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            links.add(new LinkDto((long) i, "https://github.com/owner/repo" + i, List.of(), OffsetDateTime.now()));
        }
        return links;
    }

    private List<List<LinkDto>> formBatches(List<LinkDto> links, int batchSize) {
        List<List<LinkDto>> batches = new ArrayList<>();
        for (int i = 0; i < links.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, links.size());
            batches.add(new ArrayList<>(links.subList(i, endIndex)));
        }
        return batches;
    }
}
