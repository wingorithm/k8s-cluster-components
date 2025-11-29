package com.kubeservice.a_service.client;

import com.kubeservice.a_service.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "service-b", url = "${url.b-service}")
public interface ServiceBClient {

    @GetMapping(value = "/api/books")
    List<BookResponse> getBookFromFriend();

}
