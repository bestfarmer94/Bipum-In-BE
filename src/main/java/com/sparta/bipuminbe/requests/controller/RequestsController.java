package com.sparta.bipuminbe.requests.controller;

import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.enums.UserRoleEnum;
import com.sparta.bipuminbe.requests.dto.RequestsResponseDto;
import com.sparta.bipuminbe.requests.service.RequestsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestsController {
    private final RequestsService requestsService;

//    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @GetMapping("/admin/requests")
    @Operation(summary = "요청 현황 페이지", description = "type은 NULL/SUPPLY/REPAIR/RETURN, status는 UNPROCESSED/REPAIRING/PROCESSED")
    public ResponseDto<Page<RequestsResponseDto>> getRequests(@RequestParam(required = false) String type,
                                                              @RequestParam(defaultValue = "UNPROCESSED") String status,
                                                              @RequestParam(defaultValue = "1") int page) {
        return requestsService.getRequests(type, status, page);
    }
}
