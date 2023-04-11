package com.sparta.bipuminbe.user.controller;

import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.enums.UserRoleEnum;
import com.sparta.bipuminbe.common.security.UserDetailsImpl;
import com.sparta.bipuminbe.user.dto.*;
import com.sparta.bipuminbe.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Operation(summary = "로그인 처리", description = "구글 계정정보 담은 Jwt토큰 발급")
    @PostMapping("/login/google")
    public ResponseEntity<ResponseDto<LoginResponseDto>> googleLogin(@RequestParam String code,
                                                                     @RequestParam String urlType,
                                                                     @RequestParam String ip) throws IOException {
        return userService.googleLogin(code, urlType, ip);
    }

    @Operation(summary = "로그아웃", description = "Redis refreshToken 제거.")
    @PostMapping("/logout")
    public ResponseDto<String> logout(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.logout(userDetails.getUsername());
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token 을 보내줘야 합니다.")
    @PostMapping("/reissue") // access token이 만료됐을 경우
    public ResponseDto<String> reIssueAccessToken(HttpServletResponse httpServletResponse, String ip,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.reIssueAccessToken(userDetails.getUser(), ip, httpServletResponse);
    }

    //로그인 시, 부서와 유저이름이 없는 경우 반드시 추가입력하게 유도
    @Operation(summary = "사원명, 부서 추가입력", description = "로그인 때 사원명, 부서명이 없는 경우 추가정보 입력")
    @PostMapping("/loginadd")
    public ResponseDto<LoginResponseDto> loginAdd(@Valid @RequestBody LoginRequestDto loginRequestDto,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 카카오에서 받아오지 못하는 유저이름과 부서 추가입력
        return userService.loginAdd(loginRequestDto, userDetails.getUser());
    }

    @GetMapping("/{deptId}")
    @Operation(summary = "부서별 사원 조회", description = "SelectBox용")
    public ResponseDto<List<UserResponseDto>> getUsersByDept(@PathVariable Long deptId) {
        return userService.getUserByDept(deptId);
    }

    @Operation(summary = "구글 연결 끊기", description = "앱과 연결된 구글 계정 연결 끊기")
    @PostMapping("/delete")
    public ResponseDto<String> deleteUser(HttpServletRequest request,
                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String bearerToken = request.getHeader("Authorization");

        return userService.deleteUser(userDetails.getUser(), bearerToken);
    }

    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @GetMapping("/map")
    @Operation(summary = "전체 사원 목록(비품 복수 등록 페이지)", description = "전사원 부서명(key) : 사원명(value)")
    public ResponseDto<Map<String, Set<String>>> getAllUserList() {
        return userService.getAllUserList();
    }


    @GetMapping("/myPage")
    @Operation(summary = "마이페이지", description = "유저 마이페이지")
    public ResponseDto<UserInfoResponseDto> getUserInfo(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserInfo(userDetails.getUser());
    }


    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "유저 정보 수정", description = "보이는 대로 보내주시면 될 것 같습니다.")
    public ResponseDto<String> updateUser(@ModelAttribute @Valid UserUpdateRequestDto userUpdateRequestDto,
                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return userService.updateUser(userUpdateRequestDto, userDetails.getUser());
    }

    @Operation(summary = "리프레쉬 실험(액세스)", description = "리프레쉬 실험")
    @PostMapping("/login/toy")
    public ResponseEntity<ResponseDto<LoginResponseDto>> toyLogin(@RequestParam String username,
                                                                  @RequestParam String ip) throws IOException {
        return userService.toyLogin(username, ip);
    }

    @Operation(summary = "리프레쉬 실험(재발급)", description = "리프레쉬 실험")
    @PostMapping("/reissue/toy")
    public ResponseDto<String> toyReissue(@RequestParam String username,
                                          @RequestParam String ip,
                                          HttpServletResponse httpServletResponse) throws IOException {
        return userService.toyReissue(username, ip, httpServletResponse);
    }


    //    @Operation(summary = "로그인 처리", description = "카카오 계정정보 담은 Jwt토큰 발급")
//    @PostMapping("/login")
//    public ResponseEntity<ResponseDto<LoginResponseDto>> kakaoLogin(@RequestParam String code,
//                                                                    @RequestParam String urlType) throws IOException {
//        // code: 카카오 서버로부터 받은 인가 코드
//
//        return userService.kakaoLogin(code, urlType);
//    }
}
