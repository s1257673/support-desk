package com.supportdesk.support_desk.admin.controller;

import com.supportdesk.support_desk.admin.dto.CreateAgentRequest;
import com.supportdesk.support_desk.admin.service.AdminService;
import com.supportdesk.support_desk.auth.dto.UserSummaryResponse;
import com.supportdesk.support_desk.auth.entity.Role;
import com.supportdesk.support_desk.common.responce.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // ← applies to ALL methods in this controller
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved", adminService.getAllUsers())
        );
    }

    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAgents() {
        return ResponseEntity.ok(
                ApiResponse.success("Agents retrieved", adminService.getUsersByRole(Role.AGENT))
        );
    }

    @PostMapping("/users/agent")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> createAgent(
            @Valid @RequestBody CreateAgentRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Agent created", adminService.createAgent(request))
        );
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated"));
    }
}
