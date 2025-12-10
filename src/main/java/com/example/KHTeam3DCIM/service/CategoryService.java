// CategoryService.java (도우미)
// 장비 등록 화면(HTML)을 띄울 때, 드롭다운 메뉴에 보여줄 "서버, 스위치, 스토리지" 목록을 DB에서 꺼내오는 역할입니다.

package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.KHTeam3DCIM.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 모든 카테고리 목록 가져오기
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    // (선택사항) 초기 데이터가 없으면 넣어주는 기능
    @Transactional
    public void initData() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("SVR", "Server"));
            categoryRepository.save(new Category("NET", "Network"));
            categoryRepository.save(new Category("STO", "Storage"));
            categoryRepository.save(new Category("UPS", "UPS/Power"));
        }
    }
}
