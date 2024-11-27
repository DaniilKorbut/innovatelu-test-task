import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    @Test
    public void saveWithIdTest() {
        DocumentManager manager = new DocumentManager();
        DocumentManager.Document document = getDocument(true);
        DocumentManager.Document savedDocument = manager.save(document);
        assertNotNull(savedDocument);
        assertEquals(document.getId(), savedDocument.getId());
        assertEquals(document.getTitle(), savedDocument.getTitle());
        assertEquals(document.getContent(), savedDocument.getContent());
        assertEquals(document.getCreated(), savedDocument.getCreated());
        assertNotNull(document.getAuthor());
        assertEquals(document.getAuthor().getName(), savedDocument.getAuthor().getName());
        assertEquals(document.getAuthor().getId(), savedDocument.getAuthor().getId());
    }

    @Test
    public void saveWithoutIdTest() {
        DocumentManager manager = new DocumentManager();
        DocumentManager.Document document = getDocument(false);
        DocumentManager.Document savedDocument = manager.save(document);
        assertNotNull(savedDocument);
        assertNotNull(savedDocument.getId());
        assertEquals(document.getTitle(), savedDocument.getTitle());
        assertEquals(document.getContent(), savedDocument.getContent());
        assertEquals(document.getCreated(), savedDocument.getCreated());
        assertNotNull(document.getAuthor());
        assertEquals(document.getAuthor().getName(), savedDocument.getAuthor().getName());
        assertEquals(document.getAuthor().getId(), savedDocument.getAuthor().getId());
    }

    @Test
    public void findByIdTest() {
        DocumentManager manager = new DocumentManager();
        DocumentManager.Document savedDocument = manager.save(getDocument(true));
        DocumentManager.Document document = manager.findById(savedDocument.getId()).orElse(null);
        assertNotNull(document);
        assertEquals(document.getId(), savedDocument.getId());
        assertEquals(document.getTitle(), savedDocument.getTitle());
        assertEquals(document.getContent(), savedDocument.getContent());
        assertEquals(document.getCreated(), savedDocument.getCreated());
        assertNotNull(document.getAuthor());
        assertEquals(document.getAuthor().getName(), savedDocument.getAuthor().getName());
        assertEquals(document.getAuthor().getId(), savedDocument.getAuthor().getId());

        document = manager.findById("invalid-id").orElse(null);
        assertNull(document);
    }

    @Test
    public void upsertTest() {
        DocumentManager manager = new DocumentManager();
        DocumentManager.Document savedDocument = manager.save(getDocument(false));
        DocumentManager.Document document = manager.findById(savedDocument.getId()).orElse(null);
        assertNotNull(document);
        assertEquals(document.getTitle(), savedDocument.getTitle());

        String newTitle = "Updated Shopping List";
        document.setTitle(newTitle);
        manager.save(document);
        savedDocument = manager.findById(savedDocument.getId()).orElse(null);
        assertNotNull(savedDocument);
        assertEquals(newTitle, savedDocument.getTitle());
    }

    @Test
    public void searchTest() {
        DocumentManager manager = new DocumentManager();
        List<DocumentManager.Document> documents = getDocuments();
        documents.forEach(manager::save);
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Arrays.asList("Shopping", "Monthly"))
                .build();
        List<DocumentManager.Document> searchResult = manager.search(searchRequest);
        assertEquals(2, searchResult.size());
        assertTrue(searchResult.stream().anyMatch(document -> document.getTitle().equals("Shopping List")));
        assertTrue(searchResult.stream().anyMatch(document -> document.getTitle().equals("Monthly Budget")));

        searchRequest = DocumentManager.SearchRequest.builder()
                .containsContents(Arrays.asList("Bread", "Rent"))
                .authorIds(Arrays.asList("user001", "user004"))
                .build();

        searchResult = manager.search(searchRequest);
        assertEquals(1, searchResult.size());
        assertEquals("Shopping List", searchResult.get(0).getTitle());

        searchRequest = DocumentManager.SearchRequest.builder()
                .authorIds(Arrays.asList("user001", "user002", "user004"))
                .createdFrom(Instant.parse("2022-01-01T00:00:00.00Z"))
                .createdTo(Instant.parse("2023-01-01T00:00:00.00Z"))
                .build();

        searchResult = manager.search(searchRequest);
        assertEquals( 1, searchResult.size());
        assertEquals( "Meeting Notes", searchResult.get(0).getTitle());
    }

    private DocumentManager.Document getDocument(boolean hasId) {
        return DocumentManager.Document.builder()
                .id(hasId ? UUID.randomUUID().toString() : null)
                .title("Shopping List")
                .content("Milk (2 gallons)\n" +
                         "Eggs (1 dozen)\n" +
                         "Bread (whole wheat)\n" +
                         "Apples (6)\n" +
                         "Chicken breasts (2 lbs)\n" +
                         "Laundry detergent")
                .author(DocumentManager.Author.builder()
                        .id("user001")
                        .name("Harper L.")
                .build())
                .created(Instant.parse("2021-07-15T09:23:11.87Z"))
                .build();
    }

    private List<DocumentManager.Document> getDocuments() {
        List<DocumentManager.Document> documents = new ArrayList<>();
        documents.add(getDocument(true));
        documents.add(
                DocumentManager.Document.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Meeting Notes")
                        .content("Discussed quarterly sales goals.\n" +
                                 "Team is behind on Q3 targets; strategies proposed: increased advertising and discounts.\n" +
                                 "Follow-up task: John to finalize marketing plan by Dec 5.\n" +
                                 "Next meeting scheduled for Dec 10.")
                        .author(DocumentManager.Author.builder()
                                .id("user002")
                                .name("Ezra Kinsley")
                                .build())
                        .created(Instant.parse("2022-03-04T17:45:26.03Z"))
                        .build()
        );
        documents.add(
                DocumentManager.Document.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Monthly Budget")
                        .content("Rent: $1,200\n" +
                                 "Utilities: $300\n" +
                                 "Groceries: $450\n" +
                                 "Entertainment: $150\n" +
                                 "Miscellaneous: $200\n" +
                                 "Total Expenses: $2,300")
                        .author(DocumentManager.Author.builder()
                                .id("user003")
                                .name("Marissa Vail")
                                .build())
                        .created(Instant.parse("2022-11-19T14:12:58.42Z"))
                        .build()
        );
        documents.add(
                DocumentManager.Document.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Workout Plan")
                        .content("Monday: Cardio (30 min), Upper body strength\n" +
                                 "Wednesday: Yoga (45 min)\n" +
                                 "Friday: Cardio (20 min), Lower body strength\n" +
                                 "Saturday: Hiking or swimming")
                        .author(DocumentManager.Author.builder()
                                .id("user004")
                                .name("Joaquin Mercer")
                                .build())
                        .created(Instant.parse("2023-05-23T08:10:14.99Z"))
                        .build()
        );
        return documents;
    }

}
