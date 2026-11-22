package com.supportdesk.support_desk.ticket.controller;

import com.supportdesk.support_desk.common.responce.ApiResponse;
import com.supportdesk.support_desk.ticket.dto.TicketCategoryRequest;
import com.supportdesk.support_desk.ticket.dto.TicketCategoryResponse;
import com.supportdesk.support_desk.ticket.service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = @Controller + @ResponseBody
//   Every method return value is automatically serialized to JSON
//
// @RequestMapping → base URL prefix for all methods in this class

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService categoryService;

    // GET /api/categories → all categories (admin use)\
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketCategoryResponse>>> getAllCategories() {
        List<TicketCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", categories)
        );
    }

    // GET /api/categories/active → active categories only (customer-facing)
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TicketCategoryResponse>>> getActiveCategories() {
        List<TicketCategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(
                ApiResponse.success("Active categories retrieved successfully", categories)
        );
    }

    // GET /api/categories/{id} → single category
    // @PathVariable → extracts {id} from the URL
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> getCategoryById(
            @PathVariable Long id) {
        TicketCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", category)
        );
    }

    // POST /api/categories → create new category
    // @RequestBody  → deserializes the JSON request body into TicketCategoryRequest
    // @Valid        → triggers bean validation (@NotBlank, @Size etc.)
    //                 if validation fails → MethodArgumentNotValidException
    //                 → caught by GlobalExceptionHandler → 400 response
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> createCategory(
            @Valid @RequestBody TicketCategoryRequest request) {
        TicketCategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)   // 201 Created
                .body(ApiResponse.success("Category created successfully", created));
    }

    // PUT /api/categories/{id} → full update
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TicketCategoryRequest request) {
        TicketCategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", updated)
        );
    }

    // DELETE /api/categories/{id} → soft delete (deactivate)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateCategory(
            @PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category deactivated successfully")
        );
    }
}
