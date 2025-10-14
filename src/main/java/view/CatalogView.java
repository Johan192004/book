package view;

import domain.SessionContext;

public class CatalogView {
    
    
    public void display() {
        if(SessionContext.getCurrentUser() != null) {
            // Display catalog based on user role
            if (SessionContext.getCurrentUser().getRole() == domain.User.Role.ADMIN) {
                // Display admin catalog view
            } else if (SessionContext.getCurrentUser().getRole() == domain.User.Role.ASSISTANT) {
                // Display assistant catalog view
            }
        } else {
            // Handle case where no user is logged in
        }
    }

    private void displayAdminCatalog() {
        // Implementation for admin catalog view
    }
}
