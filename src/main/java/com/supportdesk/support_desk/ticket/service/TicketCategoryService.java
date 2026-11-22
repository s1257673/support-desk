package com.supportdesk.support_desk.ticket.service;

import com.supportdesk.support_desk.common.exception.BadRequestException;
import com.supportdesk.support_desk.common.exception.ResourceNotFoundException;
import com.supportdesk.support_desk.ticket.dto.TicketCategoryRequest;
import com.supportdesk.support_desk.ticket.dto.TicketCategoryResponse;
import com.supportdesk.support_desk.ticket.entity.TicketCategory;
import com.supportdesk.support_desk.ticket.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service         → marks this as a Spring service component (business logic layer)
// @RequiredArgsConstructor → Lombok generates a constructor for all 'final' fields.
//                            Spring sees one constructor → auto-injects the dependency.
//                            This replaces @Autowired on the field.
// @Transactional   → all public methods in this class run inside a database transaction.
//                    If any exception is thrown, the transaction rolls back automatically.

@Service
@RequiredArgsConstructor
@Transactional
public class TicketCategoryService {

    // 'final' + @RequiredArgsConstructor = constructor injection (best practice)
    private final TicketCategoryRepository categoryRepository;

 
    // READ — no need to modify database, use readOnly=true
    // This tells Hibernate: skip dirty-checking, minor performance gain
 
    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)   // convert each entity to DTO
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketCategoryResponse getCategoryById(Long id) {
        TicketCategory category = findCategoryOrThrow(id);
        return mapToResponse(category);
    }

 
    // CREATE
 
    public TicketCategoryResponse createCategory(TicketCategoryRequest request) {

        // Business rule: no duplicate category names
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException(
                    "Category with name '" + request.getName() + "' already exists"
            );
        }

        TicketCategory category = TicketCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TicketCategory saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

 
    // UPDATE
 
    public TicketCategoryResponse updateCategory(Long id, TicketCategoryRequest request) {

        TicketCategory category = findCategoryOrThrow(id);

        // Check name uniqueness only if the name is actually changing
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException(
                    "Category with name '" + request.getName() + "' already exists"
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        // No need to call save() explicitly here.
        // Because this method is @Transactional, Hibernate tracks changes to
        // 'category' (a managed entity). At the end of the transaction,
        // it automatically issues an UPDATE — this is called "dirty checking".
        return mapToResponse(category);
    }

 
    // SOFT DELETE — we don't remove from DB, just deactivate
 
    public void deactivateCategory(Long id) {
        TicketCategory category = findCategoryOrThrow(id);
        category.setIsActive(false);
        // Again, dirty checking handles the UPDATE automatically
    }

 
    // PRIVATE HELPERS — reusable inside this service only
 
    private TicketCategory findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "id", id)
                );
    }

    // Maps Entity → Response DTO
    // Private helper so it's reused consistently across all methods
    private TicketCategoryResponse mapToResponse(TicketCategory category) {
        return TicketCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}