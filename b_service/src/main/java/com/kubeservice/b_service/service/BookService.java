package com.kubeservice.b_service.service;

import com.kubeservice.b_service.client.LogServiceClient;
import com.kubeservice.b_service.client.ServiceAClient;
import com.kubeservice.b_service.dto.BookResponse;
import com.kubeservice.b_service.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LogServiceClient logServiceClient;
    private final ServiceAClient serviceBClient;

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
            return logServiceClient.getBookFromFriend();
        } else {
            return serviceBClient.getBookFromFriend();
        }
    }
}