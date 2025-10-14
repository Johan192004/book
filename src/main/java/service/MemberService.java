package service;

import java.util.List;

import dao.MemberDao;
import domain.Member;
import domain.User;
import errors.*;
import util.Logger;

public class MemberService {
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * Create a new member
     * @param name Member name
     * @param email Member email
     * @param phone Member phone
     * @param userRole Role of the user performing the action (ADMIN or ASSISTANT)
     * @return Created member
     * @throws BadRequestException if validation fails
     * @throws ConflictException if email or phone already exists
     * @throws UnauthorizedException if user doesn't have permission
     * @throws ServiceException if database error occurs
     */
    public Member createMember(String name, String email, String phone, User.Role userRole) {
        try {
            // Validate permissions - both ADMIN and ASSISTANT can create members
            validatePermissionForCreate(userRole);
            
            // Check if email already exists
            Member existingMemberByEmail = memberDao.findByEmail(email);
            if (existingMemberByEmail != null) {
                throw new ConflictException("A member with this email already exists");
            }
            
            // Check if phone already exists
            Member existingMemberByPhone = memberDao.findByPhone(phone);
            if (existingMemberByPhone != null) {
                throw new ConflictException("A member with this phone number already exists");
            }
            
            // Create new member
            Member newMember = new Member(name, email, phone);
            Member savedMember = memberDao.save(newMember);
            
            Logger.info("MemberService", String.format("Member created successfully - ID: %d, Email: %s by %s", 
                savedMember.getId(), savedMember.getEmail(), userRole.name()));
            
            return savedMember;
            
        } catch (DataAccessException e) {
            Logger.logException("MemberService", "Error creating member", e);
            throw new ServiceException("Error creating member", e);
        }
    }

    /**
     * Delete a member by ID
     * @param memberId Member ID to delete
     * @param userRole Role of the user performing the action
     * @return true if deletion was successful
     * @throws UnauthorizedException if user doesn't have permission
     * @throws NotFoundException if member doesn't exist
     * @throws ServiceException if database error occurs
     */
    public boolean deleteMember(int memberId, User.Role userRole) {
        try {
            // Only ADMIN can delete members
            validatePermissionForDelete(userRole);
            
            // Check if member exists
            Member member = memberDao.findById(memberId);
            if (member == null) {
                throw new NotFoundException("Member not found with ID: " + memberId);
            }
            
            boolean deleted = memberDao.delete(memberId);
            
            if (deleted) {
                Logger.info("MemberService", String.format("Member deleted successfully - ID: %d by %s", 
                    memberId, userRole.name()));
            }
            
            return deleted;
            
        } catch (DataAccessException e) {
            Logger.logException("MemberService", "Error deleting member", e);
            throw new ServiceException("Error deleting member", e);
        }
    }

    /**
     * Update member information
     * @param memberId Member ID to update
     * @param name New name
     * @param email New email
     * @param phone New phone
     * @param userRole Role of the user performing the action
     * @return Updated member
     */
    public Member updateMember(int memberId, String name, String email, String phone, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can update members
            validatePermissionForCreate(userRole);
            
            // Check if member exists
            Member member = memberDao.findById(memberId);
            if (member == null) {
                throw new NotFoundException("Member not found with ID: " + memberId);
            }
            
            // Check if email already exists for another member
            Member existingMemberByEmail = memberDao.findByEmail(email);
            if (existingMemberByEmail != null && existingMemberByEmail.getId() != memberId) {
                throw new ConflictException("A member with this email already exists");
            }
            
            // Check if phone already exists for another member
            Member existingMemberByPhone = memberDao.findByPhone(phone);
            if (existingMemberByPhone != null && existingMemberByPhone.getId() != memberId) {
                throw new ConflictException("A member with this phone number already exists");
            }
            
            // Update member data
            member.setName(name);
            member.setEmail(email);
            member.setPhone(phone);
            
            boolean updated = memberDao.update(member);
            
            if (!updated) {
                throw new ServiceException("Failed to update member", null);
            }
            
            Logger.info("MemberService", String.format("Member updated successfully - ID: %d by %s", 
                memberId, userRole.name()));
            
            return member;
            
        } catch (DataAccessException e) {
            Logger.logException("MemberService", "Error updating member", e);
            throw new ServiceException("Error updating member", e);
        }
    }

    /**
     * Get all members
     * @param userRole Role of the user performing the action
     * @return List of all members
     */
    public List<Member> getAllMembers(User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view members
            validatePermissionForCreate(userRole);

            List<Member> members = memberDao.findAll();

            if (members == null || members.isEmpty()) {
                throw new NotFoundException("No members found");
            }

            return members;

        } catch (DataAccessException e) {
            Logger.logException("MemberService", "Error getting all members", e);
            throw new ServiceException("Error getting all members", e);
        }
    }

    /**
     * Find member by ID
     * @param memberId Member ID
     * @param userRole Role of the user performing the action
     * @return Member if found
     */
    public Member findMemberById(int memberId, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view members
            validatePermissionForCreate(userRole);
            
            Member member = memberDao.findById(memberId);
            if (member == null) {
                throw new NotFoundException("Member not found with ID: " + memberId);
            }
            
            return member;
            
        } catch (DataAccessException e) {
            Logger.logException("MemberService", "Error finding member by ID", e);
            throw new ServiceException("Error finding member", e);
        }
    }

    /**
     * Validate permission for creating/updating members
     * Both ADMIN and ASSISTANT can perform these actions
     */
    private void validatePermissionForCreate(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        // Both ADMIN and ASSISTANT have permission to create/update
        Logger.info("MemberService", String.format("Permission validated for create/update - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for deleting members
     * Only ADMIN can perform this action
     */
    private void validatePermissionForDelete(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete members");
        }
        
        Logger.info("MemberService", String.format("Permission validated for delete - Role: %s", userRole.name()));
    }
}
