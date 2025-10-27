package com.example.fas.specifications;

import com.example.fas.model.User;
import com.example.fas.enums.Status;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils; // Helper của Spring, kiểm tra null/rỗng/khoảng trắng
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class này chứa các "bản thiết kế" query động cho User Entity.
 */
public class UserSpecification {

    /**
     * Hàm tĩnh này nhận các tham số tìm kiếm (có thể null)
     * và "lắp ráp" chúng thành một Specification hoàn chỉnh.
     * * @param username (Tùy chọn) Tên đăng nhập để tìm kiếm (gần đúng - LIKE)
     * @param email (Tùy chọn) Email để tìm kiếm (gần đúng - LIKE)
     * @param status (Tùy chọn) Trạng thái để tìm kiếm (chính xác - EQUAL)
     * @return một Specification<User>
     */
    public static Specification<User> searchUsers(
            String username, String fullName,
            String email, Status status,
            String identityCard, String phoneNumber,
            String role) {

        // (root, query, criteriaBuilder) ->
        // Đây là một biểu thức Lambda. Hãy hiểu 3 tham số này:
        // 1. root: Đại diện cho bảng 'User' (FROM User). Dùng để lấy cột (vd: root.get("username"))
        // 2. query: Không dùng nhiều ở đây, dùng cho các query phức tạp (group by, order by)
        // 3. cb (CriteriaBuilder): "Nhà máy" để tạo ra các điều kiện (vd: cb.like(), cb.equal())
        return (root, query, cb) -> {

            // List để chứa các "mệnh đề" (Predicate)
            List<Predicate> predicates = new ArrayList<>();

            // --- Điều kiện 1: Tìm theo 'username' ---
            if (StringUtils.hasText(username)) { // Tốt hơn '!= null'
                // Thêm mệnh đề: WHERE lower(username) LIKE '%username_da_viet_thuong%'
                // cb.lower(): Chuyển cột 'username' về chữ thường
                // cb.like(): Tạo điều kiện LIKE
                // "%...%": Nghĩa là tìm "gần đúng" (chỉ cần chứa chuỗi)
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }

            // --- Điều kiện 2: Tìm theo 'email' ---
            if (StringUtils.hasText(email)) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            // --- Điều kiện 3: Tìm theo 'status' ---
            if (status != null) { // Enum chỉ cần kiểm tra null
                // Thêm mệnh đề: AND status = 'STATUS' (tìm chính xác)
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (identityCard != null) {
                predicates.add(cb.equal(root.get("identityCard"), identityCard));
            }

            // Kết hợp tất cả các mệnh đề lại bằng AND
            // (Nếu list 'predicates' rỗng, nó sẽ trả về 1=1 - tức là lấy tất cả)
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}