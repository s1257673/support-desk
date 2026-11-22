package com.supportdesk.support_desk.auth.dto;


import com.supportdesk.support_desk.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

// A lightweight user representation used INSIDE other responses.
// We never expose the full User entity (which has passwordHash & other sensitive things).

@Getter
@Builder
public class UserSummaryResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}