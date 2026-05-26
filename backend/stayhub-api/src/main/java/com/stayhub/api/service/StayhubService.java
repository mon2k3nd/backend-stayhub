package com.stayhub.api.service;

import com.stayhub.api.entity.*;
import com.stayhub.api.repository.*;
import com.stayhub.api.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // 🟢 HÀM ĐĂNG NHẬP (LOGIN)
    public AuthResponse login(String identifier, String password) {
        User user = userRepo.findByPhoneNumber(identifier)
                .orElseGet(() -> userRepo.findAll().stream()
                        .filter(u -> identifier.equalsIgnoreCase(u.getEmail()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản có số điện thoại hoặc email này!")));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác. Vui lòng thử lại!");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRoleId());

        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setToken(token);
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRoleId());
        response.setPlan(user.getPackageId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCccdNumber(user.getCccdNumber());
        response.setHometown(user.getHometown());
        response.setGender(user.getGender());

        return response;
    }

    // 🟢 HÀM ĐĂNG KÝ (REGISTER)
    @Transactional
    public String registerUser(String name, String password, String phoneNumber, String email, String appType) {
        Optional<User> existingUser = userRepo.findByPhoneNumber(phoneNumber);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Số điện thoại này đã được đăng ký trên hệ thống!");
        }

        String finalEmail = (email == null || email.isBlank()) ? "no-email-" + phoneNumber + "@stayhub.com" : email;
        String resolvedRole = "TENANT";
        String resolvedPackage = "FREE";

        if ("OWNER_APP".equalsIgnoreCase(appType)) {
            resolvedRole = "OWNER";
            resolvedPackage = "FREE";
        } else if ("TENANT_APP".equalsIgnoreCase(appType)) {
            boolean isDeclared = tenantRepo.findAll().stream()
                    .anyMatch(t -> phoneNumber.equals(t.getPhoneNumber()));

            if (!isDeclared) {
                throw new RuntimeException("Số điện thoại của bạn chưa được Chủ trọ khai báo vào danh sách phòng! Vui lòng liên hệ Chủ trọ trước.");
            }
            resolvedRole = "TENANT";
            resolvedPackage = "FREE";
        }

        User newUser = User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(finalEmail)
                .password(encoder.encode(password))
                .roleId(resolvedRole)
                .packageId(resolvedPackage)
                .isRequestingOwner(false)
                .build();

        userRepo.save(newUser);
        return "Đăng ký thành công tài khoản vai trò: " + resolvedRole;
    }

    // 🟢 CÁC HÀM NGHIỆP VỤ KHÁC
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    @Transactional
    public Room createRoom(Long ownerId, Room room, List<String> roomImageUrls, List<String> inspectionImageUrls) {
        return roomRepo.save(room);
    }

    public List<TenantProfile> getAllTenants() {
        return tenantRepo.findAll();
    }

    @Transactional
    public TenantProfile createTenant(TenantProfile tenant) {
        return tenantRepo.save(tenant);
    }

    public List<Bill> getAllBills(Long roomId) {
        return billRepo.findAll();
    }

    @Transactional
    public Bill createBill(Bill bill) {
        return billRepo.save(bill);
    }

    public List<CashCollection> getAllCashCollections() {
        return cashRepo.findAll();
    }

    @Transactional
    public CashCollection createCashCollection(CashCollection cash) {
        return cashRepo.save(cash);
    }

    public List<OwnerTenantMapping> getAllMappings() {
        return mappingRepo.findAll();
    }

    // 🔥 ĐÃ SỬA: Xóa bỏ hoàn toàn dấu chấm vô tình dính ở đây giúp hết lỗi syntax expected!
    @Transactional
    public OwnerTenantMapping createMapping(OwnerTenantMapping mapping) {
        return mappingRepo.save(mapping);
    }

    public List<MaintenanceRequest> getAllMaintenanceRequests() {
        return maintenanceRepo.findAll();
    }

    @Transactional
    public MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request) {
        return maintenanceRepo.save(request);
    }

    public List<MaintenanceRequest> getMaintenanceByStaff(Long staffId) {
        List<MaintenanceRequest> results = new ArrayList<>();
        for (MaintenanceRequest req : maintenanceRepo.findAll()) {
            if (Objects.equals(req.getStaffId(), staffId)) {
                results.add(req);
            }
        }
        return results;
    }

    @Transactional
    public MaintenanceRequest ownerAssignStaff(Long requestId, Long staffId) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu sửa chữa!"));
        request.setStaffId(staffId);
        request.setStatus(com.stayhub.api.entity.RequestStatus.PROCESSING);
        return maintenanceRepo.save(request);
    }

    @Transactional
    public MaintenanceRequest staffUpdateStatus(Long requestId, String statusStr) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu sửa chữa để cập nhật!"));

        try {
            com.stayhub.api.entity.RequestStatus newStatus =
                    com.stayhub.api.entity.RequestStatus.valueOf(statusStr.toUpperCase());
            request.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái bảo trì '" + statusStr + "' không hợp lệ trên hệ thống!");
        }

        return maintenanceRepo.save(request);
    }

    @Transactional
    public void ownerDeleteMaintenance(Long requestId, Long ownerId) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu yêu cầu cần xóa!"));
        maintenanceRepo.delete(request);
    }
}