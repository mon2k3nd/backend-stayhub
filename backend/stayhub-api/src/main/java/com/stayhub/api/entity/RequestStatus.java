package com.stayhub.api.entity;

public enum RequestStatus {
    PENDING,     // Yêu cầu mới gửi, chờ xử lý
    PROCESSING,  // Đang trong quá trình sửa chữa (Chủ nhà đã giao việc cho Staff)
    DONE,        // Đã sửa chữa xong hoàn toàn
    CANCELLED    // Yêu cầu bị hủy (Do bấm nhầm hoặc không cần thiết nữa)
}