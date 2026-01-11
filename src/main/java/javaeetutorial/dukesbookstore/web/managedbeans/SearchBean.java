package javaeetutorial.dukesbookstore.web.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import javaeetutorial.dukesbookstore.ejb.BookRequestBean;
import javaeetutorial.dukesbookstore.entity.Book;

@Named("searchBean")
@SessionScoped
public class SearchBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(SearchBean.class.getName());
    private static final int PAGE_SIZE = 10;

    @Inject
    private BookRequestBean bookRequestBean;

    private String searchQuery;
    private List<Book> searchResults;
    private int currentPage = 1;
    private int totalPages = 1;

    public SearchBean() {
        this.searchResults = new ArrayList<>();
    }

    public String performSearch() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            searchResults = new ArrayList<>();
            return null;
        }

        String sanitizedQuery = sanitizeInput(searchQuery.trim());
        logger.info("Searching for books with query: " + sanitizedQuery);

        try {
            List<Book> allResults = bookRequestBean.searchBooksByTitle(sanitizedQuery);
            totalPages = (int) Math.ceil((double) allResults.size() / PAGE_SIZE);
            currentPage = 1;
            searchResults = paginateResults(allResults, currentPage);
        } catch (Exception e) {
            logger.severe("Error during book search: " + e.getMessage());
            searchResults = new ArrayList<>();
        }

        return "searchResults";
    }

    private String sanitizeInput(String input) {
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "");
    }

    private List<Book> paginateResults(List<Book> allResults, int page) {
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allResults.size());

        if (start >= allResults.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(allResults.subList(start, end));
    }

    public void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            try {
                List<Book> allResults = bookRequestBean.searchBooksByTitle(sanitizeInput(searchQuery));
                searchResults = paginateResults(allResults, currentPage);
            } catch (Exception e) {
                logger.severe("Error fetching next page: " + e.getMessage());
            }
        }
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            try {
                List<Book> allResults = bookRequestBean.searchBooksByTitle(sanitizeInput(searchQuery));
                searchResults = paginateResults(allResults, currentPage);
            } catch (Exception e) {
                logger.severe("Error fetching previous page: " + e.getMessage());
            }
        }
    }

    // Getters and Setters
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Book> getSearchResults() {
        return searchResults;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNextPage() {
        return currentPage < totalPages;
    }

    public boolean isHasPreviousPage() {
        return currentPage > 1;
    }
}

