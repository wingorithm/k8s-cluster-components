package com.logservice.logservice.service;

import com.logservice.logservice.client.ServiceAClient;
import com.logservice.logservice.client.ServiceBClient;
import com.logservice.logservice.dto.BookResponse;
import com.logservice.logservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ServiceAClient serviceAClient;
    private final ServiceBClient serviceBClient;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor()
                ))
                .toList();
    }

    public List<BookResponse> getBooksFromFriend(int friendId) {
        if (friendId == 0) {
            return serviceAClient.getBookFromFriend();
        } else {
            return serviceBClient.getBookFromFriend();
        }
    }
}