// CategoryRepository.java
// 장비 등록할 때 드롭다운 메뉴("서버", "스위치"...)를 띄워주기 위해 필요합니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
