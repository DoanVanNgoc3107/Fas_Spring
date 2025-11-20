package com.example.fas.config;

import com.example.fas.model.Role;
import com.example.fas.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataInitializer: Tự động tạo các role mặc định khi ứng dụng khởi động
 *
 * Quy trình:
 * 1. Kiểm tra xem đã có role trong database chưa
 * 2. Nếu chưa → tạo 2 role ADMIN và USER
 * 3. Nếu có rồi → bỏ qua (tránh duplicate)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            long roleCount = roleRepository.count();

            if (roleCount == 0) {
                logger.info("🔄 Bắt đầu tạo các role mặc định...");

                // Tạo role ADMIN
                Role adminRole = new Role();
                adminRole.setRoleName("ADMIN");
                adminRole.setDescription("Administrator role - Quản trị viên hệ thống");
                roleRepository.save(adminRole);
                logger.info("✅ Role ADMIN đã được tạo");

                // Tạo role USER
                Role userRole = new Role();
                userRole.setRoleName("USER");
                userRole.setDescription("Regular user role - Người dùng thường");
                roleRepository.save(userRole);
                logger.info("✅ Role USER đã được tạo");

                logger.info("✅ 2 role (ADMIN, USER) đã được tạo tự động!");
            } else {
                logger.info("✓ Roles đã tồn tại trong database (count: {}), bỏ qua việc tạo mới", roleCount);
            }
        } catch (Exception e) {
            logger.error("❌ Lỗi khi khởi tạo roles: {}", e.getMessage(), e);
        }
    }
}

