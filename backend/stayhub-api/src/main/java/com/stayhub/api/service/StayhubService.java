package com.stayhub.api.service;

import com.stayhub.api.entity.*;
import com.stayhub.api.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    // Constructor thuần giúp chạy mượt mà trên mọi phiên bản Java, không lo xung đột
    public StayhubService(UserRepository userRepo, RoomRepository roomRepo, TenantProfileRepository tenantRepo,
                          BillRepository billRepo, CashCollectionRepository cashRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.tenantRepo = tenantRepo;
        this.billRepo = billRepo;
        this.cashRepo = cashRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // --- LOGIC 1: ĐĂNG KÝ PHÂN GÓI SAAS ---
    public String registerOwner(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);
        user.setCurrentPlan(PlanType.FREE);
        user.setRoomLimit(5);
        userRepo.save(user);
        return "Đăng ký tài khoản hệ thống StayHub thành công!";
    }

    public Map<String, String> login(String email, String password) {
        User u = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));
        if (!encoder.matches(password, u.getPassword())) throw new RuntimeException("Sai tài khoản hoặc mật khẩu");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", u.getRole().name());
        claims.put("ownerId", u.getRole() == Role.ADMIN ? u.getId() : u.getOwnerId());

        String token = jwtService.generateToken(u.getEmail(), claims);
        Map<String, String> res = new HashMap<>();
        res.put("token", token);
        res.put("role", u.getRole().name());
        return res;
    }

    // --- LOGIC 2: KIỂM TRA HẠN MỨC GÓI KHI THÊM PHÒNG ---
    public Room addRoom(Room room, Long ownerId) {
        User owner = userRepo.findById(ownerId).orElseThrow();
        long currentRooms = roomRepo.countByOwnerId(ownerId);

        if (owner.getCurrentPlan() == PlanType.FREE && currentRooms >= owner.getRoomLimit()) {
            throw new RuntimeException("Bạn đã đạt giới hạn 5 phòng của gói FREE. Vui lòng nâng cấp lên PRO hoặc VIP!");
        }
        room.setOwnerId(ownerId);
        room.setStatus(RoomStatus.TRONG);
        return roomRepo.save(room);
    }

    // --- LOGIC 3: ĐỊNH DANH CỐ ĐỊNH NGƯỜI Ở [Mã_Tòa]-[Số_Phòng]-[STT] ---
    public TenantProfile registerTenantProfile(TenantProfile profile, Long roomId, Long ownerId) {
        Room room = roomRepo.findById(roomId).orElseThrow();
        long currentSubTenants = tenantRepo.countByRoomId(roomId);

        String formattedCode = String.format("%s-%s-%02d", room.getBuildingCode(), room.getRoomNumber(), currentSubTenants + 1);

        profile.setTenantCode(formattedCode);
        profile.setRoomId(roomId);
        profile.setOwnerId(ownerId);
        return tenantRepo.save(profile);
    }

    // --- LOGIC 4: UP CHỈ SỐ VÀ TỰ ĐỘNG SINH VIETQR KHI CHỦ BẤM CHỐT ---
    public Bill inputMeterIndices(Long roomId, Integer oldElec, Integer newElec, String elecImg, Integer oldWater, Integer newWater, String waterImg, Long ownerId) {
        // --- ĐOẠN THÊM MỚI: CHẶN CHỈ SỐ ÂM ĐỂ BẢO VỆ DOANH THU ---
        if (newElec < oldElec) {
            throw new IllegalArgumentException("Lỗi: Số điện mới (" + newElec + ") không được nhỏ hơn số điện cũ (" + oldElec + ")!");
        }
        if (newWater < oldWater) {
            throw new IllegalArgumentException("Lỗi: Số nước mới (" + newWater + ") không được nhỏ hơn số nước cũ (" + oldWater + ")!");
        }
        // --- HẾT ĐOẠN THÊM MỚI ---

        Room room = roomRepo.findById(roomId).orElseThrow();
        double total = room.getPrice() + ((newElec - oldElec) * room.getElectricityPrice()) + ((newWater - oldWater) * room.getWaterPrice()) + room.getServiceFee();

        Bill bill = new Bill();
        bill.setRoomId(roomId);
        bill.setOwnerId(ownerId);
        bill.setBillingMonth(LocalDate.now());
        bill.setOldElectricityNumber(oldElec);
        bill.setNewElectricityNumber(newElec);
        bill.setElectricityMeterImageUrl(elecImg);
        bill.setOldWaterNumber(oldWater);
        bill.setNewWaterNumber(newWater);
        bill.setWaterMeterImageUrl(waterImg);
        bill.setBaseRoomPrice(room.getPrice());
        bill.setPenaltyFee(0.0);
        bill.setTotalAmount(total);
        bill.setApprovedByOwner(false);
        bill.setPaid(false);

        return billRepo.save(bill);
    }

    public Bill approveAndGenerateVietQR(Long billId) {
        Bill bill = billRepo.findById(billId).orElseThrow();
        Room room = roomRepo.findById(bill.getRoomId()).orElseThrow();

        bill.setApprovedByOwner(true);
        String dynamicQR = "https://img.vietqr.io/image/MBBank-0123456789-qr_only.png?amount="
                + bill.getTotalAmount().intValue() + "&addInfo=StayHub%20Thanh%20Toan%20Phong%20" + room.getRoomNumber();
        bill.setQrCodeUrl(dynamicQR);
        return billRepo.save(bill);
    }

    // --- LOGIC 5: NHÂN VIÊN THU TIỀN MẶT VÀ CHỦ NHÀ ĐỐI SOÁT QUỸ ---
    public CashCollection staffCollectCash(Long staffId, Long roomId, Double amount, Long ownerId) {
        CashCollection cc = new CashCollection();
        cc.setStaffId(staffId);
        cc.setRoomId(roomId);
        cc.setOwnerId(ownerId);
        cc.setAmountCollected(amount);
        cc.setCollectedAt(LocalDateTime.now());
        cc.setHandedOverToOwner(false);
        return cashRepo.save(cc);
    }

    public void ownerConfirmHandover(Long staffId) {
        List<CashCollection> pendingCollections = cashRepo.findByStaffIdAndIsHandedOverToOwner(staffId, false);
        for (CashCollection c : pendingCollections) {
            c.setHandedOverToOwner(true);
            cashRepo.save(c);
        }
    }

    // --- LOGIC 6: TASK CHẠY NGẦM TỰ ĐỘNG PHẠT 50K/NGÀY NẾU QUÁ HẠN ĐÓNG TIỀN ---
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoApplyPenalty() {
        List<Bill> unpaidBills = billRepo.findByIsPaid(false);
        for (Bill b : unpaidBills) {
            if (b.isApprovedByOwner()) {
                b.setPenaltyFee(b.getPenaltyFee() + 50000);
                b.setTotalAmount(b.getTotalAmount() + 50000);

                Room r = roomRepo.findById(b.getRoomId()).orElseThrow();
                String updateQR = "https://img.vietqr.io/image/MBBank-0123456789-qr_only.png?amount="
                        + b.getTotalAmount().intValue() + "&addInfo=StayHub%20Phat%20Tre%20Han%20Phong%20" + r.getRoomNumber();
                b.setQrCodeUrl(updateQR);
                billRepo.save(b);
            }
        }
    }

    // --- LOGIC 7: THỐNG KÊ BIẾN ĐỘNG NHÂN KHẨU HỌC (GÓI VIP) ---
    public Map<String, Object> getVIPDemographicsDashboard(Long ownerId) {
        User owner = userRepo.findById(ownerId).orElseThrow();
        if (owner.getCurrentPlan() != PlanType.VIP) {
            throw new RuntimeException("Tính năng thống kê chuyên sâu chỉ dành cho tài khoản nâng cấp gói VIP!");
        }
        List<TenantProfile> list = tenantRepo.findByOwnerId(ownerId);
        Map<String, Integer> genderMap = new HashMap<>();
        Map<String, Integer> hometownMap = new HashMap<>();

        for (TenantProfile p : list) {
            genderMap.put(p.getGender(), genderMap.getOrDefault(p.getGender(), 0) + 1);
            hometownMap.put(p.getHometown(), hometownMap.getOrDefault(p.getHometown(), 0) + 1);
        }
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTenants", list.size());
        statistics.put("genderChartData", genderMap);
        statistics.put("hometownChartData", hometownMap);
        return statistics;
    }
}