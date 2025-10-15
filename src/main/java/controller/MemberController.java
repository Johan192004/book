package controller;

import domain.Member;
import domain.User;
import errors.*;
import service.MemberService;
import util.Logger;
import util.TableFormatter;

import java.util.HashMap;
import java.util.List;

public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Create a new member
     * @param name Member name
     * @param email Member email
     * @param phone Member phone
     * @param userRole Role of the user performing the action
     * @return HashMap with status and message
     */
    public HashMap<String, String> createMember(String name, String email, String phone, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("MemberController", String.format("Create member attempt - Email: %s, Role: %s", email, userRole));
        
        try {
            // Validate input data in controller
            validateMemberInput(name, email, phone);
            
            Member member = memberService.createMember(name, email, phone, userRole);
            
            response.put("status", "201");
            response.put("message", "Member created successfully");
            response.put("id", String.valueOf(member.getId()));
            response.put("name", member.getName());
            response.put("email", member.getEmail());
            response.put("phone", member.getPhone());
            
            Logger.info("MemberController", String.format("[201] Member created successfully - ID: %d", member.getId()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[400] Create member failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[401] Create member failed - Unauthorized: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[409] Create member failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("MemberController", "[500] Create member error", e);
        }
        
        return response;
    }

    /**
     * Delete a member
     * @param memberId Member ID to delete
     * @param userRole Role of the user performing the action
     * @return HashMap with status and message
     */
    public HashMap<String, String> deleteMember(int memberId, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("MemberController", String.format("Delete member attempt - ID: %d, Role: %s", memberId, userRole));
        
        try {
            boolean deleted = memberService.deleteMember(memberId, userRole);
            
            if (deleted) {
                response.put("status", "200");
                response.put("message", "Member deleted successfully");
                Logger.info("MemberController", String.format("[200] Member deleted successfully - ID: %d", memberId));
            } else {
                response.put("status", "500");
                response.put("message", "Failed to delete member");
                Logger.warn("MemberController", String.format("[500] Delete member failed - ID: %d", memberId));
            }
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[401] Delete member failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[404] Delete member failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("MemberController", "[500] Delete member error", e);
        }
        
        return response;
    }

    /**
     * Update a member
     * @param memberId Member ID to update
     * @param name New name
     * @param email New email
     * @param phone New phone
     * @param userRole Role of the user performing the action
     * @return HashMap with status and message
     */
    public HashMap<String, String> updateMember(int memberId, String name, String email, String phone, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("MemberController", String.format("Update member attempt - ID: %d, Role: %s", memberId, userRole));
        
        try {
            // Validate member ID
            if (memberId <= 0) {
                throw new BadRequestException("Member ID must be a positive number");
            }
            
            // Validate input data in controller
            validateMemberInput(name, email, phone);
            
            Member member = memberService.updateMember(memberId, name, email, phone, userRole);
            
            response.put("status", "200");
            response.put("message", "Member updated successfully");
            response.put("id", String.valueOf(member.getId()));
            response.put("name", member.getName());
            response.put("email", member.getEmail());
            response.put("phone", member.getPhone());
            
            Logger.info("MemberController", String.format("[200] Member updated successfully - ID: %d", member.getId()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[400] Update member failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[401] Update member failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[404] Update member failed - Not found: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[409] Update member failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("MemberController", "[500] Update member error", e);
        }
        
        return response;
    }

    /**
     * Get all members
     * @param userRole Role of the user performing the action
     * @return HashMap with status and message (message contains formatted table)
     */
    public HashMap<String, String> getAllMembers(User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("MemberController", String.format("Get all members attempt - Role: %s", userRole));
        
        try {
            List<Member> members = memberService.getAllMembers(userRole);
            
            // Format members as table and put in message
            String tableMessage = TableFormatter.formatMembersTable(members);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("MemberController", String.format("[200] Members retrieved successfully - Count: %d", members.size()));
            
        }catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[404] Get all members failed - Not found: %s", e.getMessage()));

        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[401] Get all members failed - Unauthorized: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("MemberController", "[500] Get all members error", e);
        }
        
        return response;
    }

    /**
     * Find member by ID
     * @param memberId Member ID to find
     * @param userRole Role of the user performing the action
     * @return HashMap with status and message (message contains formatted details)
     */
    public HashMap<String, String> findMemberById(int memberId, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("MemberController", String.format("Find member by ID attempt - ID: %d, Role: %s", memberId, userRole));
        
        try {
            Member member = memberService.findMemberById(memberId, userRole);
            
            // Format member details and put in message
            String detailsMessage = TableFormatter.formatMemberDetails(member);
            
            response.put("status", "200");
            response.put("message", detailsMessage);
            
            Logger.info("MemberController", String.format("[200] Member found - ID: %d", memberId));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[401] Find member failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("MemberController", String.format("[404] Find member failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("MemberController", "[500] Find member error", e);
        }
        
        return response;
    }

    /**
     * Validate member input data (basic validation)
     * Validates that fields are not null, empty, and meet format requirements
     * @param name Member name
     * @param email Member email
     * @param phone Member phone
     * @throws BadRequestException if validation fails
     */
    private void validateMemberInput(String name, String email, String phone) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Name cannot be null or empty");
        }
        
        if (name.trim().length() > 255) {
            throw new BadRequestException("Name cannot exceed 255 characters");
        }
        
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be null or empty");
        }
        
        if (email.trim().length() > 255) {
            throw new BadRequestException("Email cannot exceed 255 characters");
        }
        
        // Basic email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid email format");
        }
        
        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Phone cannot be null or empty");
        }
        
        if (phone.trim().length() > 15) {
            throw new BadRequestException("Phone cannot exceed 15 characters");
        }
        
        // Basic phone format validation (only digits, spaces, +, -, ())
        if (!phone.matches("^[0-9\\s+\\-()]+$")) {
            throw new BadRequestException("Invalid phone format. Only digits, spaces, +, -, () are allowed");
        }
    }
}
