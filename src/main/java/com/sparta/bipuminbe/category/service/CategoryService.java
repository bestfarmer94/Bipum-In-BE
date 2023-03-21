package com.sparta.bipuminbe.category.service;

import com.sparta.bipuminbe.category.dto.CategoryDto;
import com.sparta.bipuminbe.category.repository.CategoryRepository;
import com.sparta.bipuminbe.common.dto.ResponseDto;
import com.sparta.bipuminbe.common.entity.Category;
import com.sparta.bipuminbe.common.enums.LargeCategory;
import com.sparta.bipuminbe.common.exception.CustomException;
import com.sparta.bipuminbe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public ResponseDto<List<CategoryDto>> getCategoryList(String largeCategory) {
        Set<LargeCategory> categoryQuery = getCategoryQuery(largeCategory);
        List<Category> categoryList = categoryRepository.findByLargeCategoryIn(categoryQuery);
        List<CategoryDto> categoryDtoList = new ArrayList<>();
        for (Category category : categoryList) {
            categoryDtoList.add(CategoryDto.of(category));
        }
        return ResponseDto.success(categoryDtoList);
    }

    private Set<LargeCategory> getCategoryQuery(String largeCategory) {
        Set<LargeCategory> categoryQuery = new HashSet<>();
        if (largeCategory.equals("ALL")) {
            categoryQuery.addAll(List.of(LargeCategory.values()));
        } else {
            categoryQuery.add(LargeCategory.valueOf(largeCategory));
        }
        return categoryQuery;
    }

    @Transactional
    public ResponseDto<String> createCategory(CategoryDto categoryDto) {
        if (checkCategory(categoryDto.getCategoryName())) {
            throw new CustomException(ErrorCode.DuplicatedCategory);
        }
        categoryRepository.save(Category.builder().categoryDto(categoryDto).build());
        return ResponseDto.success("카테고리 등록 완료.");
    }

    @Transactional
    public ResponseDto<String> updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = getCategory(categoryId);
        if (!categoryDto.getCategoryName().equals(category.getCategoryName()) && checkCategory(categoryDto.getCategoryName())) {
            throw new CustomException(ErrorCode.DuplicatedCategory);
        }
        category.update(categoryDto);
        return ResponseDto.success("카테고리 수정 완료.");
    }

    @Transactional
    public ResponseDto<String> deleteCategory(Long categoryId) {
        categoryRepository.delete(getCategory(categoryId));
        return ResponseDto.success("카테고리 삭제 완료.");
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.NotFoundCategory));
    }

    private Boolean checkCategory(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }
}
