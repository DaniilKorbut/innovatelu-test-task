import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> storage = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            return null;
        }

        String id = document.getId() == null ? UUID.randomUUID().toString() : document.getId();
        Instant created = document.getCreated() == null ? Instant.now() : document.getCreated();
        if (document.getId() != null) {
            Document existingDocument = findById(document.getId()).orElse(null);
            if (existingDocument != null) {
                created = existingDocument.getCreated();
                storage.remove(existingDocument);
            }
        }

        Document savedDocument = Document.builder()
                .id(id)
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(created)
                .build();

        storage.add(savedDocument);
        return savedDocument;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.stream()
                .filter(document -> matchesSearchRequest(request, document))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return storage.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();
    }

    private boolean matchesSearchRequest(SearchRequest request, Document document) {
        return isTitlePrefixed(request.titlePrefixes, document.getTitle())
               && isContainsContents(request.containsContents, document.getContent())
               && isAuthorPresent(request.authorIds, document.getAuthor())
               && isCreatedWithinInterval(request.createdFrom, request.createdTo, document.getCreated());
    }

    private boolean isTitlePrefixed(List<String> titlePrefixes, String title) {
        if (titlePrefixes == null || titlePrefixes.isEmpty()) {
            return true;
        }
        if (title == null) {
            return false;
        }
        return titlePrefixes.stream().anyMatch(title::startsWith);
    }

    private boolean isContainsContents(List<String> contents, String content) {
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        if (content == null) {
            return false;
        }
        return contents.stream().anyMatch(content::contains);
    }

    private boolean isAuthorPresent(List<String> authorIds, Author author) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        if (author == null || author.getId() == null) {
            return false;
        }
        return authorIds.contains(author.getId());
    }

    private boolean isCreatedWithinInterval(Instant createdFrom, Instant createdTo, Instant created) {
        boolean matchCreatedFrom = createdFrom == null || (created != null && !created.isBefore(createdFrom));
        boolean matchCreatedTo = createdTo == null || (created != null && !created.isAfter(createdTo));
        return matchCreatedFrom && matchCreatedTo;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}