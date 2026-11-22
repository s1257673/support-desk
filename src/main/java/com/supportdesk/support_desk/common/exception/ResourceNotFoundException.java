package com.supportdesk.support_desk.common.exception;

// extends RuntimeException → unchecked exception
// We don't need to declare it in method signatures with "throws"
// Spring's @Transactional also rolls back on RuntimeException automatically

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Convenience constructor: ResourceNotFoundException("Ticket", "id", 99)
    // produces: "Ticket not found with id: 99"
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}