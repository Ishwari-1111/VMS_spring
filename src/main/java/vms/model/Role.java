package vms.model;

public enum Role {
    ADMIN("Admin"),
    VOLUNTEER("Volunteer");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
