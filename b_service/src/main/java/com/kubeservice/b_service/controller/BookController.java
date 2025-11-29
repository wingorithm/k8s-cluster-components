package com.kubeservice.b_service.controller;

import com.kubeservice.b_service.dto.BookResponse;
import com.kubeservice.b_service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookResponse> getBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{friendId}")
    public List<BookResponse> getBooksFromFriend(
            @PathVariable("friendId") int friendId
    ) {
        return bookService.getBooksFromFriend(friendId);
    }
}