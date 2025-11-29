package com.kubeservice.b_service.client;

import com.kubeservice.b_service.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "service-a", url = "${url.a-service}")
public interface ServiceAClient {

    @GetMapping(value = "/api/books")
    List<BookResponse> getBookFromFriend();

}
