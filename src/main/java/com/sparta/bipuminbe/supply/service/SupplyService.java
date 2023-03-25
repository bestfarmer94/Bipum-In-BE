package com.sparta.bipuminbe.supply.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.bipuminbe.category.repository.CategoryRepository;
import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.entity.*;
import com.sparta.bipuminbe.common.enums.SupplyStatusEnum;
import com.sparta.bipuminbe.common.exception.CustomException;
import com.sparta.bipuminbe.common.exception.ErrorCode;
import com.sparta.bipuminbe.common.s3.S3Uploader;
import com.sparta.bipuminbe.common.security.UserDetailsImpl;
import com.sparta.bipuminbe.partners.repository.PartnersRepository;
import com.sparta.bipuminbe.requests.repository.RequestsRepository;
import com.sparta.bipuminbe.supply.dto.*;
import com.sparta.bipuminbe.supply.repository.SupplyRepository;
import com.sparta.bipuminbe.user.repository.UserRepository;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyService {
    private final SupplyRepository supplyRepository;
    private final UserRepository userRepository;
    private final PartnersRepository partnersRepository;
    private final RequestsRepository requestsRepository;
    private final CategoryRepository categoryRepository;

    @Value("${Naver.Client.ID}")
    private String naverClientId;
    @Value("${Naver.Client.Secret}")
    private String naverClientSecret;

    private final S3Uploader s3Uploader;

    //비품 등록
    @Transactional
    public ResponseDto<String> createSupply(SupplyRequestDto supplyRequestDto) throws IOException {

        Partners partners = null;
        if (supplyRequestDto.getPartnersId() != null) {
            partners = partnersRepository.findById(supplyRequestDto.getPartnersId()).orElseThrow(
                    () -> new CustomException(ErrorCode.NotFoundPartners)
            );
        }

        User user = null;
        if (supplyRequestDto.getUserId() != null) {
            user = userRepository.findById(supplyRequestDto.getUserId()).orElseThrow(
                    () -> new CustomException(ErrorCode.NotFoundUsers)
            );
        }

        String fileImg = s3Uploader.uploadFiles(supplyRequestDto.getMultipartFile(), supplyRequestDto.getCategoryName());

        Optional<Category> category = categoryRepository.findByCategoryName(supplyRequestDto.getCategoryName());

        Category newCategory = null;
        if (category.isPresent()) {
            newCategory = category.get();
        } else {
            newCategory = Category.builder().largeCategory(supplyRequestDto.getLargeCategory())
                    .categoryName(supplyRequestDto.getCategoryName()).build();
            categoryRepository.save(newCategory);
        }

        Supply newSupply = new Supply(supplyRequestDto, partners, newCategory, user, fileImg);
        supplyRepository.save(newSupply);

        return ResponseDto.success("비품 등록 성공");
    }


    //비품 조회
    @Transactional(readOnly = true)
    public ResponseDto<Page<SupplyResponseDto>> getSupplyList(String keyword, String categoryId, String status, int page, int size) {
        Set<Long> categoryQuery = getCategoryQuerySet(categoryId);
        Set<SupplyStatusEnum> statusQuery = getStatusSet(status);
        Pageable pageable = getPageable(page, size);

        Page<Supply> supplies = supplyRepository.getSupplyList("%" + keyword + "%", categoryQuery, statusQuery, pageable);
        List<SupplyResponseDto> supplyResponseDtoList = converToDto(supplies.getContent());
        return ResponseDto.success(new PageImpl<>(supplyResponseDtoList, supplies.getPageable(), supplies.getTotalElements()));
    }

    private Set<Long> getCategoryQuerySet(String categoryId) {
        Set<Long> categoryQuerySet = new HashSet<>();
        if (categoryId.equals("")) {
            List<Category> categoryList = categoryRepository.findAll();
            for (Category category : categoryList) {
                categoryQuerySet.add(category.getId());
            }
        } else {
            Category category = categoryRepository.findById(Long.parseLong(categoryId)).orElseThrow(
                    () -> new CustomException(ErrorCode.NotFoundCategory));
            categoryQuerySet.add(category.getId());
        }
        return categoryQuerySet;
    }

    // list 추출 조건용 requestStatus Set 리스트.
    private Set<SupplyStatusEnum> getStatusSet(String status) {
        Set<SupplyStatusEnum> requestStatusQuery = new HashSet<>();
        if (status.equals("ALL")) {
            requestStatusQuery.addAll(List.of(SupplyStatusEnum.values()));
        } else {
            requestStatusQuery.add(SupplyStatusEnum.valueOf(status));
        }
        return requestStatusQuery;
    }

    private Pageable getPageable(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(page - 1, size, sort);
    }

    private List<SupplyResponseDto> converToDto(List<Supply> supplyList) {
        List<SupplyResponseDto> supplyDtoList = new ArrayList<>();
        for (Supply supply : supplyList) {
            supplyDtoList.add(SupplyResponseDto.of(supply));
        }
        return supplyDtoList;
    }

//    //비품 조회
//    @Transactional(readOnly = true)
//    public ResponseDto<SupplyCategoryDto> getSupplyCategory(Long categoryId) {
//        List<Category> categoryList = categoryRepository.findAll();
//        List<Supply> supplyList = supplyRepository.findByCategory_Id(categoryId);
//        List<SupplyResponseDto> supplyDtoList = new ArrayList<>();
//        for (Supply supply : supplyList) {
//            supplyDtoList.add(SupplyResponseDto.of(supply));
//        }
//        List<CategoryDto> categoryDtoList = new ArrayList<>();
//        for (Category category : categoryList) {
//            categoryDtoList.add(CategoryDto.of(category));
//        }
//
//        SupplyCategoryDto supplyCategory = SupplyCategoryDto.of(categoryDtoList,supplyDtoList);
//        return ResponseDto.success(supplyCategory);
//    }


    //비품 상세
    @Transactional(readOnly = true)
    public ResponseDto<SupplyWholeResponseDto> getSupply(Long supplyId) {

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundSupply)
        );
        SupplyDetailResponseDto supplyDetail = new SupplyDetailResponseDto(supply);
        List<SupplyHistoryResponseDto> historyList = new ArrayList<>();
        List<SupplyRepairHistoryResponseDto> repairHistoryList = new ArrayList<>();
        List<Requests> requests = requestsRepository.findBySupply(supply);
        for (Requests request : requests) {
            historyList.add(new SupplyHistoryResponseDto(request));
            repairHistoryList.add(new SupplyRepairHistoryResponseDto(request.getSupply()));
        }
        SupplyWholeResponseDto supplyWhole = SupplyWholeResponseDto.of(supplyDetail, historyList, repairHistoryList);
        return ResponseDto.success(supplyWhole);
    }

    //유저 할당
    @Transactional
    public ResponseDto<String> updateSupply(Long supplyId, Long userId) {

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundSupply)
        );

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundUsers)
        );

        //Todo 여기 관리자 권한을 이미 Controller에서 Secured로 확인 했어서 필요없어 보입니다.
//        if (supply.getUser() != user) {
//            throw new CustomException(ErrorCode.NoPermission);
//        }

        supply.allocateSupply(user);
        return ResponseDto.success("비품 수정 성공");
    }


    //비품 폐기
    @Transactional
    public ResponseDto<String> deleteSupply(Long supplyId) {

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundSupply)
        );
        supplyRepository.delete(supply);
        return ResponseDto.success("비품 삭제 성공");
    }


    //자신의 비품 목록(selectbox용)
    @Transactional(readOnly = true)
    public ResponseDto<List<SupplyUserDto>> getSupplyUser(User user) {
        List<Supply> supplyInUserList = supplyRepository.findByUser(user);
        List<SupplyUserDto> supplyUserDtoList = new ArrayList<>();
        for (Supply supply : supplyInUserList) {
            supplyUserDtoList.add(SupplyUserDto.of(supply));
        }
        return ResponseDto.success(supplyUserDtoList);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundUsers));
    }

    @Transactional(readOnly = true)
    public ResponseDto<List<StockSupplyResponseDto>> getStockSupply(Long categoryId) {
        List<Supply> stockSupplyList = supplyRepository.findByCategory_IdAndStatus(categoryId, SupplyStatusEnum.STOCK);
        List<StockSupplyResponseDto> stockSupplyResponseDtoList = new ArrayList<>();
        for (Supply supply : stockSupplyList) {
            stockSupplyResponseDtoList.add(StockSupplyResponseDto.of(supply));
        }
        return ResponseDto.success(stockSupplyResponseDtoList);
    }

    @Transactional(readOnly = true)
    public ResponseDto<ImageResponseDto> getImageByNaver(String modelName) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("X-Naver-Client-Id", naverClientId);
        headers.add("X-Naver-Client-Secret", naverClientSecret);
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = rest.exchange("https://openapi.naver.com/v1/search/shop.json?display=1&query=" + modelName, HttpMethod.GET, requestEntity, String.class);

        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        log.info("NAVER API Status Code : " + status);

        String response = responseEntity.getBody();

        return ResponseDto.success(fromJSONtoItems(response));
    }

    private ImageResponseDto fromJSONtoItems(String response) {
        JSONObject rjson = new JSONObject(response);
        JSONArray items = rjson.getJSONArray("items");
        if (items.length() == 0) {
            throw new CustomException(ErrorCode.InValidRequest);
        }
        return ImageResponseDto.of(items.getJSONObject(0));
    }
}
