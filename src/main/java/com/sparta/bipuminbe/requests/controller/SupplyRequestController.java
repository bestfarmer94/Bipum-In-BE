package com.sparta.bipuminbe.requests.controller;

import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.enums.AcceptResult;
import com.sparta.bipuminbe.common.enums.UserRoleEnum;
import com.sparta.bipuminbe.common.security.UserDetailsImpl;
import com.sparta.bipuminbe.common.sse.service.NotificationService;
import com.sparta.bipuminbe.requests.dto.SupplyRequestResponseDto;
import com.sparta.bipuminbe.requests.service.SupplyRequestService;
import com.sparta.bipuminbe.supply.service.SupplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupplyRequestController {

    private final SupplyRequestService supplyRequestService;
    private final NotificationService notificationService;

    @GetMapping("/requests/supply/{requestId}")
    @Operation(summary = "비품 요청 상세 페이지", description = "isAdmin 필드에 따라 버튼 바꿔주면 될 것 같습니다.")
    public ResponseDto<SupplyRequestResponseDto> getSupplyRequest(@PathVariable Long requestId,
                                    @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return supplyRequestService.getSupplyRequest(requestId, userDetails.getUser());
    }

    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @PutMapping("/requests/supply/{requestId}")
    @Operation(summary = "비품 요청 승인/거절", description = "acceptResult 승인/거부/폐기 ACCEPT/DECLINE/DISPOSE, " +
            "승인의 경우 supplyId 같이 필요. 관리자 권한 필요.")
    public ResponseDto<String> processingSupplyRequest(@PathVariable Long requestId,
                                                       @RequestParam String acceptResult,
                                                       @RequestParam(required = false) Long supplyId) {

        // 관리자의 요청 처리 >> 요청자에게 알림 전송.
        // uri는 해당 알림을 클릭하면 이동할 상세페이지 uri이다.
//        String uri = "/api/requests/supply/";
//        notificationService.send(requestId, isAccepted, uri);

        return supplyRequestService.processingSupplyRequest(requestId, AcceptResult.valueOf(acceptResult), supplyId);
    }
}
