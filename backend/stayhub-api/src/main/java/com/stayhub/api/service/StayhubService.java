package com.stayhub.api.service;

import com.stayhub.api.entity.*;
import com.stayhub.api.repository.*;
import com.stayhub.api.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    // ============================================================
    // AUTH
    // ============================================================

    public AuthResponse login(String identifier, String password) {
        User user = userRepo.findByPhoneNumber(identifier)
                .or(() -> userRepo.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với: " + identifier));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRoleId());

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRoleId())
                .plan(user.getPackageId())
                .name(user.getName())
                .email(user.getEmail())
                .cccdNumber(user.getCccdNumber())
                .hometown(user.getHometown())
                .gender(user.getGender())
                .build();
    }

    @Transactional
    public String registerUser(String name, String password, String phoneNumber, String email, String appType) {
        if (userRepo.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new RuntimeException("Số điện thoại này đã được đăng ký!");
        }

        String finalEmail = (email == null || email.isBlank())
                ? "no-email-" + phoneNumber + "@stayhub.com" : email;

        String resolvedRole = "TENANT";

        if ("OWNER_APP".equalsIgnoreCase(appType)) {
            resolvedRole = "OWNER";
        } else if ("TENANT_APP".equalsIgnoreCase(appType)) {
            boolean isDeclared = tenantRepo.findByPhoneNumber(phoneNumber).isPresent();
            if (!isDeclared) {
                throw new RuntimeException("Số điện thoại chưa được Chủ trọ khai báo! Liên hệ chủ nhà để được thêm vào hệ thống.");
            }
        }

        User newUser = User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(finalEmail)
                .password(encoder.encode(password))
                .roleId(resolvedRole)
                .packageId("FREE")
                .isRequestingOwner(false)
                .build();

        userRepo.save(newUser);
        return "Đăng ký thành công với vai trò: " + resolvedRole;
    }

    // ============================================================
    // USER PROFILE
    // ============================================================

    @Transactional
    public User updateUserProfile(Long userId, String name, String email, String cccdNumber, String hometown, String gender) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));
        if (name != null && !name.isBlank()) user.setName(name);
        if (email != null && !email.isBlank()) user.setEmail(email);
        user.setCccdNumber(cccdNumber);
        user.setHometown(hometown);
        user.setGender(gender);
        return userRepo.save(user);
    }

    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

    // ============================================================
    // ROOMS
    // ============================================================

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public List<Room> getRoomsByOwner(Long ownerId) {
        return roomRepo.findByOwnerId(ownerId);
    }

    @Transactional
    public Room createRoom(Long ownerId, Room room, List<String> roomImageUrls, List<String> inspectionImageUrls) {
        room.setOwnerId(ownerId);
        if (roomImageUrls != null && !roomImageUrls.isEmpty()) {
            room.setRoomImages(String.join(",", roomImageUrls));
        }
        if (inspectionImageUrls != null && !inspectionImageUrls.isEmpty()) {
            room.setInspectionImages(String.join(",", inspectionImageUrls));
        }
        return roomRepo.save(room);
    }

    @Transactional
    public Room updateRoom(Long roomId, Long ownerId, Room updatedData) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + roomId));
        if (!room.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa phòng này!");
        }
        if (updatedData.getRoomName() != null) room.setRoomName(updatedData.getRoomName());
        if (updatedData.getPrice() != null) room.setPrice(updatedData.getPrice());
        if (updatedData.getAddress() != null) room.setAddress(updatedData.getAddress());
        if (updatedData.getDescription() != null) room.setDescription(updatedData.getDescription());
        if (updatedData.getStatus() != null) room.setStatus(updatedData.getStatus());
        if (updatedData.getDeposit() != null) room.setDeposit(updatedData.getDeposit());
        if (updatedData.getElectricityPrice() != null) room.setElectricityPrice(updatedData.getElectricityPrice());
        if (updatedData.getWaterPrice() != null) room.setWaterPrice(updatedData.getWaterPrice());
        if (updatedData.getServiceFee() != null) room.setServiceFee(updatedData.getServiceFee());
        if (updatedData.getMaxGuests() != null) room.setMaxGuests(updatedData.getMaxGuests());
        return roomRepo.save(room);
    }

    @Transactional
    public void deleteRoom(Long roomId, Long ownerId) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + roomId));
        if (!room.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Bạn không có quyền xóa phòng này!");
        }
        roomRepo.delete(room);
    }

    // ============================================================
    // TENANTS
    // ============================================================

    public List<TenantProfile> getAllTenants() {
        return tenantRepo.findAll();
    }

    public List<TenantProfile> getTenantsByOwner(Long ownerId) {
        return tenantRepo.findByOwnerId(ownerId);
    }

    @Transactional
    public TenantProfile createTenant(TenantProfile tenant) {
        return tenantRepo.save(tenant);
    }

    @Transactional
    public void deleteTenant(Long tenantId) {
        tenantRepo.deleteById(tenantId);
    }

    // ============================================================
    // BILLS
    // ============================================================

    public List<Bill> getAllBills(Long roomId) {
        if (roomId != null) {
            return billRepo.findByRoomIdOrderByYearDescMonthDesc(roomId);
        }
        return billRepo.findAll();
    }

    public List<Bill> getUnpaidBills() {
        return billRepo.findByIsPaidFalse();
    }

    @Transactional
    public Bill createBill(Bill bill) {
        if (bill.getIsPaid() == null) bill.setIsPaid(false);
        return billRepo.save(bill);
    }

    // FIX: EXISTS query thay vì tải toàn bộ bills rồi .stream().anyMatch()
    public boolean billExistsForMonth(Long roomId, Integer month, Integer year) {
        return billRepo.existsByRoomIdAndMonthAndYear(roomId, month, year);
    }

    @Transactional
    public Bill updateBillStatus(Long billId, Boolean isPaid) {
        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn ID: " + billId));
        bill.setIsPaid(isPaid);
        return billRepo.save(bill);
    }

    // ============================================================
    // DASHBOARD SUMMARY — ĐÃ SỬA N+1 QUERY
    // ============================================================

    public Map<String, Object> getDashboardSummary(Long ownerId) {
        List<Room> rooms = roomRepo.findByOwnerId(ownerId);
        long totalRooms = rooms.size();
        long occupiedRooms = rooms.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == RoomStatus.DA_THUE)
                .count();
        long emptyRooms = totalRooms - occupiedRooms;

        // FIX: countByOwnerId thay vì findByOwnerId().size()
        long totalTenants = tenantRepo.countByOwnerId(ownerId);

        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());

        double totalRevenue = 0;
        long unpaidCount = 0;

        if (!roomIds.isEmpty()) {
            // FIX: 2 aggregate queries thay vì N queries (1 query/phòng)
            // 30 phòng trước đây = 30 lần gọi DB → giờ chỉ còn 2 lần
            totalRevenue = billRepo.sumRevenueByRoomIdIn(roomIds);
            unpaidCount = billRepo.countUnpaidByRoomIdIn(roomIds);
        }

        // FIX: countByOwnerIdAndStatusIn thay vì findAll().stream().filter()
        // Trước đây tải TOÀN BỘ bảng maintenance → giờ WHERE trực tiếp trong DB
        long pendingMaintenance = maintenanceRepo.countByOwnerIdAndStatusIn(
                ownerId,
                List.of(RequestStatus.PENDING, RequestStatus.PROCESSING)
        );

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRooms", totalRooms);
        summary.put("occupiedRooms", occupiedRooms);
        summary.put("emptyRooms", emptyRooms);
        summary.put("totalTenants", totalTenants);
        summary.put("totalRevenue", totalRevenue);
        summary.put("unpaidBillCount", unpaidCount);
        summary.put("pendingMaintenanceCount", pendingMaintenance);
        return summary;
    }

    // ============================================================
    // CASH COLLECTIONS
    // ============================================================

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

    @Transactional
    public OwnerTenantMapping createMapping(OwnerTenantMapping mapping) {
        return mappingRepo.save(mapping);
    }

    // ============================================================
    // MAINTENANCE
    // ============================================================

    public List<MaintenanceRequest> getAllMaintenanceRequests() {
        return maintenanceRepo.findAll();
    }

    @Transactional
    public MaintenanceRequest tenantCreateRequest(Long tenantId, Long roomId, String title, String description) {
        Long ownerId = roomRepo.findById(roomId)
                .map(Room::getOwnerId)
                .orElse(null);

        MaintenanceRequest request = MaintenanceRequest.builder()
                .tenantId(tenantId)
                .roomId(roomId)
                .ownerId(ownerId)
                .title(title)
                .description(description)
                .status(RequestStatus.PENDING)
                .build();

        return maintenanceRepo.save(request);
    }

    public List<MaintenanceRequest> getMaintenanceForOwner(Long ownerId, String statusStr, String search) {
        RequestStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = RequestStatus.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                status = null;
            }
        }
        String cleanSearch = (search != null && search.isBlank()) ? null : search;
        return maintenanceRepo.searchAndFilterForOwner(ownerId, status, cleanSearch);
    }

    // FIX: Thêm method lấy maintenance theo tenantId (trước đây bị thiếu)
    public List<MaintenanceRequest> getMaintenanceForTenant(Long tenantId) {
        return maintenanceRepo.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional
    public MaintenanceRequest ownerAssignStaff(Long requestId, Long staffId) {
        MaintenanceRequest req = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + requestId));
        req.setStaffId(staffId);
        req.setStatus(RequestStatus.PROCESSING);
        return maintenanceRepo.save(req);
    }

    @Transactional
    public MaintenanceRequest staffUpdateStatus(Long requestId, String statusStr) {
        MaintenanceRequest req = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + requestId));
        try {
            req.setStatus(RequestStatus.valueOf(statusStr.toUpperCase().trim()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr
                    + ". Hợp lệ: PENDING, PROCESSING, DONE, CANCELLED");
        }
        return maintenanceRepo.save(req);
    }

    @Transactional
    public void ownerDeleteMaintenance(Long requestId, Long ownerId) {
        MaintenanceRequest req = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + requestId));
        if (!ownerId.equals(req.getOwnerId())) {
            throw new RuntimeException("Bạn không có quyền xóa yêu cầu này!");
        }
        maintenanceRepo.delete(req);
    }
}