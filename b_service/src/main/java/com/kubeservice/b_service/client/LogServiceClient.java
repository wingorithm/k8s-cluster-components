package com.kubeservice.b_service.client;

import com.kubeservice.b_service.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "logservice", url = "${url.logservice}")
public interface LogServiceClient {

    @GetMapping(value = "/api/logservice/books")
    List<BookResponse> getBookFromFriend();

}
