package com.logservice.logservice.client;

import com.logservice.logservice.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "service-b", url = "${url.b-service}")
public interface ServiceBClient {

    @GetMapping(value = "/api/b-service/books")
    List<BookResponse> getBookFromFriend();

}
