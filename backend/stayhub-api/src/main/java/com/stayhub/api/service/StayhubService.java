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

    public AuthResponse login(String identifier, String password) {
        User user = userRepo.findByPhoneNumber(identifier)
                .orElseGet(() -> userRepo.findAll().stream()
                        .filter(u -> identifier.equalsIgnoreCase(u.getEmail()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan!")));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mat khau khong chinh xac!");
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

    @Transactional
    public String registerUser(String name, String password, String phoneNumber, String email, String appType) {
        if (userRepo.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new RuntimeException("So dien thoai nay da duoc dang ky!");
        }

        String finalEmail = (email == null || email.isBlank())
                ? "no-email-" + phoneNumber + "@stayhub.com" : email;
        String resolvedRole = "TENANT";

        if ("OWNER_APP".equalsIgnoreCase(appType)) {
            resolvedRole = "OWNER";
        } else if ("TENANT_APP".equalsIgnoreCase(appType)) {
            boolean isDeclared = tenantRepo.findAll().stream()
                    .anyMatch(t -> phoneNumber.equals(t.getPhoneNumber()));
            if (!isDeclared) {
                throw new RuntimeException("So dien thoai chua duoc Chu tro khai bao!");
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
        return "Dang ky thanh cong voi vai tro: " + resolvedRole;
    }

    public List<User> getAllUsers() { return userRepo.findAll(); }
    public List<Room> getAllRooms() { return roomRepo.findAll(); }

    @Transactional
    public Room createRoom(Long ownerId, Room room, List<String> roomImageUrls, List<String> inspectionImageUrls) {
        return roomRepo.save(room);
    }

    public List<TenantProfile> getAllTenants() { return tenantRepo.findAll(); }

    @Transactional
    public TenantProfile createTenant(TenantProfile tenant) { return tenantRepo.save(tenant); }

    public List<Bill> getAllBills(Long roomId) {
        if (roomId != null) {
            return billRepo.findAll().stream()
                    .filter(b -> roomId.equals(b.getRoomId()))
                    .toList();
        }
        return billRepo.findAll();
    }

    @Transactional
    public Bill createBill(Bill bill) { return billRepo.save(bill); }

    @Transactional
    public Bill updateBillStatus(Long billId, Boolean isPaid) {
        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoa don ID: " + billId));
        bill.setIsPaid(isPaid);
        return billRepo.save(bill);
    }

    public List<CashCollection> getAllCashCollections() { return cashRepo.findAll(); }

    @Transactional
    public CashCollection createCashCollection(CashCollection cash) { return cashRepo.save(cash); }

    public List<OwnerTenantMapping> getAllMappings() { return mappingRepo.findAll(); }

    @Transactional
    public OwnerTenantMapping createMapping(OwnerTenantMapping mapping) { return mappingRepo.save(mapping); }

    public List<MaintenanceRequest> getAllMaintenanceRequests() { return maintenanceRepo.findAll(); }

    @Transactional
    public MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request) {
        return maintenanceRepo.save(request);
    }

    // FIX: Them moi - Tenant gui bao hong
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

    // FIX: Them moi - Owner lay danh sach + loc theo status va search
    public List<MaintenanceRequest> getMaintenanceForOwner(Long ownerId, String status, String search) {
        return maintenanceRepo.findAll().stream()
                .filter(r -> ownerId.equals(r.getOwnerId()))
                .filter(r -> {
                    if (status == null || status.isBlank()) return true;
                    try {
                        return RequestStatus.valueOf(status.toUpperCase()) == r.getStatus();
                    } catch (IllegalArgumentException e) {
                        return true;
                    }
                })
                .filter(r -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.toLowerCase();
                    return (r.getTitle() != null && r.getTitle().toLowerCase().contains(q))
                            || (r.getDescription() != null && r.getDescription().toLowerCase().contains(q));
                })
                .toList();
    }

    public List<MaintenanceRequest> getMaintenanceByStaff(Long staffId) {
        return maintenanceRepo.findAll().stream()
                .filter(r -> Objects.equals(r.getStaffId(), staffId))
                .toList();
    }

    @Transactional
    public MaintenanceRequest ownerAssignStaff(Long requestId, Long staffId) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay yeu cau sua chua!"));
        request.setStaffId(staffId);
        request.setStatus(RequestStatus.PROCESSING);
        return maintenanceRepo.save(request);
    }

    @Transactional
    public MaintenanceRequest staffUpdateStatus(Long requestId, String statusStr) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay yeu cau de cap nhat!"));
        try {
            request.setStatus(RequestStatus.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trang thai '" + statusStr + "' khong hop le!");
        }
        return maintenanceRepo.save(request);
    }

    @Transactional
    public void ownerDeleteMaintenance(Long requestId, Long ownerId) {
        MaintenanceRequest request = maintenanceRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay yeu cau can xoa!"));
        maintenanceRepo.delete(request);
    }
}