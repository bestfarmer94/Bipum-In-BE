package com.sparta.bipuminbe.requests.controller;

import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.enums.UserRoleEnum;
import com.sparta.bipuminbe.common.security.UserDetailsImpl;
import com.sparta.bipuminbe.requests.dto.RequestsRequestDto;
import com.sparta.bipuminbe.requests.dto.RequestsDetailsResponseDto;
import com.sparta.bipuminbe.requests.dto.RequestsProcessRequestDto;
import com.sparta.bipuminbe.requests.dto.RequestsPageResponseDto;
import com.sparta.bipuminbe.requests.service.RequestsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestsController {
    private final RequestsService requestsService;

    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @GetMapping("/admin/requests")
    @Operation(summary = "요청 현황 페이지", description = "keyword는 필수 x.<br> " +
            "type은 **ALL/SUPPLY/REPAIR/RETURN/REPORT**.<br> " +
            "status는 **ALL/UNPROCESSED/PROCESSING/PROCESSED**.<br> " +
            "ALL(전체조회) or 키워드x 일 때는 쿼리 안날려도 되긴함. 관리자 권한 필요.")
    public ResponseDto<Page<RequestsPageResponseDto>> getRequestsPage(@RequestParam(defaultValue = "") String keyword,
                                                                  @RequestParam(defaultValue = "ALL") String type,
                                                                  @RequestParam(defaultValue = "ALL") String status,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam (defaultValue = "10") int size) {
        return requestsService.getRequestsPage(keyword, type, status, page, size);
    }

    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @PutMapping("/admin/requests")
    @Operation(summary = "요청 승인/거절/폐기", description = "acceptResult 승인/거절/폐기, ACCEPT/DECLINE/DISPOSE. " +
            "비품요청의 승인의 경우 supplyId도 같이 필요. " +
            "거절시 거절 사유(comment) 작성 필수. 관리자 권한 필요.")
    public ResponseDto<String> processingRequests(@RequestBody @Valid RequestsProcessRequestDto requestsProcessRequestDto) {

        // 관리자의 요청 처리 >> 요청자에게 알림 전송.
        // uri는 해당 알림을 클릭하면 이동할 상세페이지 uri이다.
//        String uri = "/api/requests/";
//        notificationService.send(requestId, accep, uri);

        return requestsService.processingRequests(requestsProcessRequestDto);
    }

    @GetMapping("/requests/{requestId}")
    @Operation(summary = "요청서 상세 페이지",
            description = "isAdmin/requestType/requestStatus 필드에 따라 버튼 바꿔주시면 될 것 같습니다.")
    public ResponseDto<RequestsDetailsResponseDto> getRequestsDetails(@PathVariable Long requestId,
                                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return requestsService.getRequestsDetails(requestId, userDetails.getUser());
    }

    @PostMapping(value = "/requests", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "유저 요청 페이지", description = "**비품 요청**일 경우, 필요값 = categoryId, requestType, content<br>" +
            "**반납/수리/보고서 일 경우**, 필요값 = supplyId, requestType, content, multipartFile(이미지)<br>" +
            "requestType = SUPPLY / REPAIR / RETURN / REPORT")
    public ResponseDto<String> createRequests(@ModelAttribute RequestsRequestDto requestsRequestDto,
                                             @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return requestsService.createRequests(requestsRequestDto, userDetails.getUser());
    }
}
