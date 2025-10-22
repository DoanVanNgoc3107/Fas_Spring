package com.example.fas.security;

// Import các thư viện cần thiết
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull; // Để đánh dấu tham số không được null
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Để tạo "Thẻ phòng" mới
import org.springframework.security.core.context.SecurityContextHolder; // Nơi lưu "Thẻ phòng"
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Interface của CustomsUserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Để thêm chi tiết request vào "Thẻ phòng"
import org.springframework.stereotype.Component; // Đánh dấu là Bean
import org.springframework.web.filter.OncePerRequestFilter; // Loại Filter chuẩn

@Component // Đánh dấu là Bean ("viên gạch LEGO")
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Kế thừa Filter chuẩn

    private final JwtService jwtService; // Cần "Máy in vé" để đọc vé
    private final UserDetailsService userDetailsService; // Cần "Người giữ sổ" để lấy thông tin user

    // Constructor Injection: "Xin" 2 Bean cần thiết từ "Nhà máy" Spring
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        // Spring sẽ tự inject CustomsUserDetailsService của bạn vào đây
        // vì nó implement UserDetailsService
        this.userDetailsService = userDetailsService;
    }

    /**
     * Đây là "trái tim" của Người Soát Vé. Nó chạy cho mọi request.
     * Nhiệm vụ: Kiểm tra "vé" (JWT) và nếu hợp lệ, xác thực người dùng.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, // Request đến từ client
            @NonNull HttpServletResponse response, // Response trả về client
            @NonNull FilterChain filterChain // "Chuỗi lọc" để chuyển request đi tiếp
    ) throws ServletException, IOException {

        // 1. Lấy Header "Authorization" từ request
        // Đây là nơi client (Next.js/Postman) sẽ gửi "tấm vé" lên
        // theo định dạng: "Authorization: Bearer xxxxx.yyyyy.zzzzz"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Kiểm tra xem có Header không và có bắt đầu bằng "Bearer " không
        // Nếu không có hoặc sai định dạng -> không phải là vé JWT -> Bỏ qua, đi tiếp
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Cho request đi qua bộ lọc tiếp theo (có thể bị chặn bởi các quy tắc sau)
            filterChain.doFilter(request, response);
            return; // Kết thúc việc soát vé JWT ở đây cho request này
        }

        // 3. Nếu có vé đúng định dạng -> Tách lấy phần token (bỏ "Bearer ")
        jwt = authHeader.substring(7); // Lấy chuỗi token từ vị trí thứ 7

        try {
            // 4. Dùng JwtService để "đọc tên chủ vé" (extract username) từ token
            // Bước này cũng ngầm kiểm tra chữ ký của token luôn!
            username = jwtService.extractUsername(jwt);

            // 5. Kiểm tra xem đã đọc được tên VÀ người này chưa được xác thực trong "phiên" này
            // SecurityContextHolder.getContext().getAuthentication() == null
            // -> Đảm bảo chúng ta chỉ xác thực 1 lần cho mỗi request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Nếu chưa xác thực -> Dùng "Người giữ sổ" để lấy UserDetails từ CSDL
                // Chúng ta cần UserDetails để kiểm tra token và lấy quyền (ROLE_)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. Dùng JwtService để kiểm tra xem vé có hợp lệ không
                // (Username khớp? Chưa hết hạn?)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 8. Nếu vé HOÀN TOÀN HỢP LỆ -> Tạo "Thẻ Phòng" (Authentication object) mới
                    // Đây chính là bằng chứng xác thực hợp lệ cho Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // Thông tin user đã được xác thực (ai?)
                            null, // Credentials (mật khẩu) -> không cần nữa vì đã xác thực bằng token
                            userDetails.getAuthorities() // Quyền (ROLE_) của user (được làm gì?)
                    );
                    // (Optional) Thêm chi tiết về request (như IP address) vào "Thẻ Phòng"
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. QUAN TRỌNG NHẤT: Đặt "Thẻ Phòng" vào SecurityContextHolder
                    // => Báo cho *toàn bộ* Spring Security biết: "Request này OK, người dùng đã được xác thực!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // 10. Luôn luôn chuyển request cho bộ lọc tiếp theo trong chuỗi
            // Kể cả khi token không hợp lệ, các bộ lọc sau (như AuthorizationFilter) sẽ xử lý
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Nếu có lỗi khi đọc/kiểm tra token (ví dụ: hết hạn, chữ ký sai, token lỗi)
            // Log lỗi ra để debug
            // logger.error("Cannot set user authentication: {}", e); // Nên dùng logger thay vì System.err
            System.err.println("Error processing JWT: " + e.getMessage());

            // Quan trọng: Vẫn phải gọi filterChain.doFilter để request được tiếp tục xử lý
            // (Ví dụ: để GlobalExceptionHandler có thể bắt và trả về lỗi 401/403)
            filterChain.doFilter(request, response);
        }
    }
}