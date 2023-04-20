package com.sparta.bipuminbe.common.util.aop;

import com.sparta.bipuminbe.common.entity.User;
import com.sparta.bipuminbe.common.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UseTimeAop {

    private final ApiUseTimeRepository apiUseTimeRepository;

    @Around("execution(public * com.sparta.bipuminbe.category.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.common.sse.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.department.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.dashboard.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.requests.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.supply.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.user.controller.*.*(..)) ||" +
            "execution(public * com.sparta.bipuminbe.partners.controller.*.*(..))")
    public synchronized Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        // 측정 시작 시간
        long startTime = System.currentTimeMillis();

        try {
            // 핵심기능 수행
            Object output = joinPoint.proceed();
            return output;
        } finally {
            // 측정 종료 시간
            long endTime = System.currentTimeMillis();
            // 수행시간 = 종료 시간 - 시작 시간
            long runTime = endTime - startTime;

            // 로그인 회원이 없는 경우, 수행시간 기록하지 않음
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal().getClass() == UserDetailsImpl.class) {
                // 로그인 회원 정보
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                User loginUser = userDetails.getUser();

                // API 사용시간 및 DB 에 기록
                ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
                        .orElse(null);
                if (apiUseTime == null) {
                    // 로그인 회원의 기록이 없으면
                    apiUseTime = new ApiUseTime(loginUser, runTime);
                } else {
                    // 로그인 회원의 기록이 이미 있으면
                    apiUseTime.addUseTime(runTime);
                }

                // API URI 가져오기
                HttpServletRequest request = getRequest(joinPoint.getArgs());
                String apiUri = (request != null) ? request.getRequestURI() : "";

                log.info("[API Use Time] Username: " + loginUser.getUsername() + ", API URI: " + apiUri + ", " +
                        "Total Time: " + apiUseTime.getTotalTime() + " ms");
                apiUseTimeRepository.save(apiUseTime);

            }
        }
    }

    // HttpServletRequest 가져오기
    private HttpServletRequest getRequest(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest) {
                    return (HttpServletRequest) arg;
                }
            }
        }
        return null;
    }
}