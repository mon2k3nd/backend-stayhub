package com.stayhub.api.dto.response;

import com.stayhub.api.entity.User;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String roleId;
    private String packageId;
    private String accountStatus;
    private boolean isRequestingOwner;
    private String cccdNumber;
    private String hometown;
    private String gender;
    private String avatarUrl;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .packageId(user.getPackageId())
                .accountStatus(user.getAccountStatus())
                .isRequestingOwner(user.isRequestingOwner())
                .cccdNumber(user.getCccdNumber())
                .hometown(user.getHometown())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
