package com.supportdesk.support_desk.auth.dto;

import com.supportdesk.support_desk.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

// What we return after successful registration or login.
// The client stores this token and sends it on every subsequent request.

@Getter
@Builder
public class AuthResponse {

    private String token;           // the JWT — client must store this
    private String email;
    private String firstName;
    private String lastName;
    private Role role;              // client uses this to decide which UI to show
    private long expiresIn;         // milliseconds until token expires (UI can show countdown)
}
