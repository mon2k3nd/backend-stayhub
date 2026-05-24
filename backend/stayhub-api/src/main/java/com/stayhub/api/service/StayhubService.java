package com.stayhub.api.service;

import com.stayhub.api.entity.*;
import com.stayhub.api.repository.*;
import com.stayhub.api.dto.AuthResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StayhubService {
    private final UserRepository userRepo;
    private final RoomRepository roomRepo;
    private final TenantProfileRepository tenantRepo;
    private final BillRepository billRepo;
    private final CashCollectionRepository cashRepo;
    private final OwnerTenantMappingRepository mappingRepo;
    private final MaintenanceRequestRepository maintenanceRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public StayhubService(UserRepository userRepo, RoomRepository roomRepo, TenantProfileRepository tenantRepo,
                          BillRepository billRepo, CashCollectionRepository cashRepo,
                          OwnerTenantMappingRepository mappingRepo, MaintenanceRequestRepository maintenanceRepo,
                          PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.tenantRepo = tenantRepo;
        this.billRepo = billRepo;
        this.cashRepo = cashRepo;
        this.mappingRepo = mappingRepo;
        this.maintenanceRepo = maintenanceRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // --- LOGIC 1: PHÂN LUỒNG ĐĂNG KÝ CHO TỪNG APP ---
    public String registerUser(String email, String password, String phoneNumber, String appType) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email này đã được đăng ký trên hệ thống!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setPhoneNumber(phoneNumber);

        switch (appType) {
            case "OWNER_APP":
                user.setRole(Role.OWNER);
                user.setCurrentPlan(PlanType.FREE);
                user.setRoomLimit(5);
                user.setIsApprovedByAdmin(false);
                userRepo.save(user);
                return "Đăng ký tài khoản Chủ nhà thành công! Vui lòng đợi ADMIN phê duyệt kích hoạt gói FREE (Tối đa 5 phòng).";

            case "STAFF_APP":
                throw new RuntimeException("Tài khoản Nhân viên phải do Chủ nhà trực tiếp tạo lập và cấp quyền, không được tự ý đăng ký!");

            case "TENANT_APP":
                Optional<TenantProfile> profileOpt = tenantRepo.findByPhoneNumber(phoneNumber);
                if (profileOpt.isEmpty()) {
                    throw new RuntimeException("Số điện thoại này chưa được bất kỳ Chủ nhà nào khai báo trên hệ thống lưu trú. Vui lòng liên hệ Chủ nhà của bạn!");
                }
                user.setRole(Role.TENANT);
                user.setFullName(profileOpt.get().getFullName());
                User savedTenant = userRepo.save(user);

                List<OwnerTenantMapping> mappings = mappingRepo.findByTenantPhoneNumber(phoneNumber);
                for (OwnerTenantMapping m : mappings) {
                    m.setTenantUserId(savedTenant.getId());
                    mappingRepo.save(m);
                }
                return "Đăng ký tài khoản Khách thuê thành công! Hệ thống đã tự động liên kết dữ liệu phòng trọ dựa trên số điện thoại.";

            default:
                throw new RuntimeException("Loại ứng dụng (appType) không hợp lệ!");
        }
    }

    // --- LOGIC 2: ĐĂNG NHẬP CHUNG HỆ THỐNG GỘP APP (ĐÃ CHUẨN HÓA ĐÓNG GÓI ROLE) ---
    public AuthResponse login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại trên hệ thống!"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        if (user.getRole() == Role.OWNER && Boolean.FALSE.equals(user.getIsApprovedByAdmin())) {
            throw new RuntimeException("Tài khoản chủ nhà của bạn chưa được ADMIN phê duyệt hoặc đã bị tạm khóa!");
        }

        // 🌟 SỬA ĐỔI QUAN TRỌNG: Nối chuỗi "ROLE_" viết hoa để Token sinh ra mang quyền hạn hợp lệ tuyệt đối
        Map<String, Object> claims = new HashMap<>();
        String formattedRole = "ROLE_" + user.getRole().name().toUpperCase();
        claims.put("role", formattedRole);

        String token = jwtService.generateToken(user.getEmail(), claims);
        String planName = (user.getCurrentPlan() != null) ? user.getCurrentPlan().name() : "NONE";

        // Ghi log kiểm tra Token phát hành
        System.out.println("====== [STAYHUB AUTH SERVICE] ======");
        System.out.println("👉 Đăng nhập thành công cho Email: " + user.getEmail());
        System.out.println("👉 Token phát hành chứa quyền: " + formattedRole);
        System.out.println("====================================");

        return new AuthResponse(
                user.getId(),
                token,
                user.getEmail(),
                user.getRole().name(),
                planName
        );
    }

    // --- LOGIC 3: CHỦ NHÀ TẠO TÀI KHOẢN CHO NHÂN VIÊN (STAFF) ---
    public String ownerCreateStaff(String email, String password, String phoneNumber, Long ownerId) {
        User owner = userRepo.findById(ownerId).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin chủ nhà"));
        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Đối tượng xử lý phải là tài khoản Chủ Nhà!");
        }

        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email nhân viên này đã tồn tại!");
        }

        User staff = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .phoneNumber(phoneNumber)
                .role(Role.STAFF)
                .ownerId(ownerId)
                .build();

        userRepo.save(staff);
        return "Tạo tài khoản nhân viên thành công!";
    }

    // --- LOGIC 4: ADMIN DUYỆT CẤP PHÉP HOẶC NÂNG CẤP GÓI CHỦ NHÀ ---
    @Transactional
    public String adminApproveOwner(Long ownerId, String planStr) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản chủ nhà!"));

        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Đối tượng xử lý phải là tài khoản Chủ Nhà!");
        }

        PlanType plan = PlanType.valueOf(planStr.toUpperCase());
        owner.setCurrentPlan(plan);
        owner.setIsApprovedByAdmin(true);

        if (plan == PlanType.FREE) owner.setRoomLimit(5);
        else if (plan == PlanType.PRO) owner.setRoomLimit(50);
        else if (plan == PlanType.VIP) owner.setRoomLimit(Integer.MAX_VALUE);

        userRepo.save(owner);
        return "Admin đã phê duyệt thành công chủ nhà lên gói: " + plan.name();
    }

    // --- LOGIC 5: CHỦ NHÀ THÊM PHÒNG (KIỂM TRA GIỚI HẠN GÓI CƯỚC) ---
    public Room ownerAddRoom(Long ownerId, Room roomDetails) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản chủ nhà!"));

        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Chỉ có tài khoản quyền Chủ nhà (OWNER) mới được phép thêm phòng!");
        }

        long currentRoomCount = roomRepo.countByOwnerId(ownerId);
        if (currentRoomCount >= owner.getRoomLimit()) {
            throw new RuntimeException("Bạn đã đạt giới hạn tối đa số phòng được tạo (" + owner.getRoomLimit() + " phòng) của gói cước hiện tại. Vui lòng nâng cấp gói để tiếp tục!");
        }

        roomDetails.setOwnerId(ownerId);
        return roomRepo.save(roomDetails);
    }

    // --- LOGIC 6: CHỦ NHÀ KHAI BÁO NHÂN KHẨU KHÁCH THUÊ ---
    @Transactional
    public TenantProfile ownerRegisterTenant(Long ownerId, Long roomId, TenantProfile profile) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
        if (!room.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Phòng trọ này không thuộc quyền sở hữu của bạn!");
        }

        profile.setRoomId(roomId);
        profile.setOwnerId(ownerId);
        TenantProfile savedProfile = tenantRepo.save(profile);

        OwnerTenantMapping mapping = new OwnerTenantMapping();
        mapping.setOwnerId(ownerId);
        mapping.setRoomId(roomId);
        mapping.setTenantPhoneNumber(profile.getPhoneNumber());

        Optional<User> userOpt = userRepo.findByPhoneNumber(profile.getPhoneNumber());
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.TENANT) {
            mapping.setTenantUserId(userOpt.get().getId());
        }

        mappingRepo.save(mapping);
        return savedProfile;
    }

    // --- LOGIC 7: NHÂN VIÊN GHI CHỈ SỐ ĐIỆN NƯỚC ---
    public Bill staffInputBill(Long roomId, Integer oldE, Integer newE, String eImg, Integer oldW, Integer newW, String wImg, Long ownerId) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        int useE = newE - oldE;
        int useW = newW - oldW;
        if (useE < 0 || useW < 0) throw new RuntimeException("Chỉ số mới không được nhỏ hơn chỉ số cũ!");

        double totalE = useE * room.getElectricityPrice();
        double totalW = useW * room.getWaterPrice();
        double totalAmount = room.getPrice() + room.getServiceFee() + totalE + totalW;

        Bill bill = Bill.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .oldElectricity(oldE).newElectricity(newE).electricityProofUrl(eImg)
                .oldWater(oldW).newWater(newW).waterProofUrl(wImg)
                .totalAmount(totalAmount)
                .isPaid(false)
                .build();

        return billRepo.save(bill);
    }

    // --- LOGIC 8: THU TIỀN MẶT & ĐỐI SOÁT DÒNG TIỀN ---
    public CashCollection staffCollectCash(Long staffId, Long roomId, Double amount, Long ownerId) {
        CashCollection cash = CashCollection.builder()
                .staffId(staffId).roomId(roomId).ownerId(ownerId).amountCollected(amount)
                .collectedAt(LocalDateTime.now()).isHandedOverToOwner(false)
                .build();
        return cashRepo.save(cash);
    }

    @Transactional
    public void ownerConfirmHandover(Long staffId) {
        List<CashCollection> list = cashRepo.findByStaffIdAndIsHandedOverToOwnerFalse(staffId);
        for (CashCollection c : list) {
            c.setIsHandedOverToOwner(true);
            cashRepo.save(c);
        }
    }

    // --- LOGIC 9: TỰ ĐỘNG QUÉT QUÁ HẠN TIỀN PHÒNG ĐỂ THÔNG BÁO ---
    @Scheduled(cron = "0 0 8 * * ?")
    public void autoScanOverdueBills() {
        List<Bill> unpaidBills = billRepo.findByIsPaidFalse();
        for (Bill b : unpaidBills) {
            List<OwnerTenantMapping> maps = mappingRepo.findByRoomId(b.getRoomId());
            for (OwnerTenantMapping m : maps) {
                if (m.getTenantUserId() != null) {
                    User tenantUser = userRepo.findById(m.getTenantUserId()).orElse(null);
                    if (tenantUser != null) {
                        sendNotificationMock(tenantUser, "CẢNH BÁO QUÁ HẠN ĐÓNG TIỀN PHÒNG",
                                "Hóa đơn tháng " + b.getMonth() + " của bạn vẫn chưa được thanh toán. Tổng tiền: " + b.getTotalAmount() + "đ. Vui lòng hoàn thành sớm!");
                    }
                }
            }
        }
    }

    private void sendNotificationMock(User tenant, String title, String message) {
        System.out.println("\n========== >>> MOCK PUSH NOTIFICATION CENTER <<< ==========");
        System.out.println("-> Target: " + tenant.getPhoneNumber());
        System.out.println("-> Title: " + title);
        System.out.println("-> Body: " + message);
        System.out.println("=========================================================");
    }

    // --- LOGIC 11: THỐNG KÊ BIẾN ĐỘNG NHÂN KHẨU CHO GÓI VIP ---
    public Map<String, Object> getVIPDemographicsDashboard(Long ownerId) {
        User owner = userRepo.findById(ownerId).orElseThrow(() -> new RuntimeException("Không tìm thấy chủ nhà"));
        if (owner.getCurrentPlan() != PlanType.VIP) {
            throw new RuntimeException("Tính năng thống kê chuyên sâu chỉ dành cho tài khoản nâng cấp gói VIP!");
        }
        List<TenantProfile> list = tenantRepo.findByOwnerId(ownerId);
        Map<String, Integer> genderMap = new HashMap<>();
        Map<String, Integer> hometownMap = new HashMap<>();

        for (TenantProfile p : list) {
            if (p.getGender() != null) genderMap.put(p.getGender(), genderMap.getOrDefault(p.getGender(), 0) + 1);
            if (p.getHometown() != null) hometownMap.put(p.getHometown(), hometownMap.getOrDefault(p.getHometown(), 0) + 1);
        }
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTenants", list.size());
        statistics.put("genderDistribution", genderMap);
        statistics.put("hometownDistribution", hometownMap);
        return statistics;
    }

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }
}