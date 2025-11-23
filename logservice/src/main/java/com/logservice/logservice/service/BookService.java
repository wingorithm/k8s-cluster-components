package com.logservice.logservice.service;

import com.logservice.logservice.dto.BookResponse;
import com.logservice.logservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Timer;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor()
                ))
                .toList();
    }
}