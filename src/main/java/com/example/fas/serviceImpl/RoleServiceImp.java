package com.example.fas.serviceImpl;

import com.example.fas.dto.role.RoleRequestDto;
import com.example.fas.dto.role.RoleResponseDto;
import com.example.fas.dto.role.RoleUpdateDto;
import com.example.fas.exceptions.general.invalid.IdInvalidException;
import com.example.fas.exceptions.role.*;
import com.example.fas.mapper.RoleMapper;
import com.example.fas.model.Role;
import com.example.fas.repositories.RoleRepository;
import com.example.fas.services.RoleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImp(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new role.
     *
     * @param roleRequestDto data for create a new role
     * @return RoleResponseDto
     */
    @Override
    @Transactional
    public RoleResponseDto createRole(RoleRequestDto roleRequestDto) {
        validateRole(roleRequestDto);
        if (roleRepository.existsByRoleName(roleRequestDto.getRoleName().toUpperCase())) {
            throw new RoleNamExistsException("Role name already exists");
        }
        Role role = new Role();
        role.setRoleName(roleRequestDto.getRoleName().toUpperCase());
        role.setDescription(roleRequestDto.getDescription());
        return roleMapper.toDto(roleRepository.saveAndFlush(role));
    }

    /**
     * Update an existing role.
     *
     * @param roleUpdate - data for update the role
     * @return RoleResponseDto
     * @throws IdInvalidException if the role ID is invalid or not found
     */
    @Override
    @Transactional
    public RoleResponseDto updateRole(RoleUpdateDto roleUpdate) {
        validateIdRole(roleUpdate.getRoleIdUpdate());
        Role role = roleRepository.findById(roleUpdate.getRoleIdUpdate()).orElseThrow(() -> new RoleIdNotFoundException("Role ID not found"));
        validateRoleUpdate(roleUpdate);
        if (!roleUpdate.getRoleNameUpdate().equals(role.getRoleName()) && !roleUpdate.getRoleNameUpdate().isBlank()) {
            role.setRoleName(roleUpdate.getRoleNameUpdate().toUpperCase());
        }
        if (!roleUpdate.getDescriptionUpdate().equals(role.getDescription()) && !roleUpdate.getDescriptionUpdate().isBlank()) {
            role.setDescription(roleUpdate.getDescriptionUpdate());
        }
        return roleMapper.toDto(roleRepository.saveAndFlush(role));
    }

    /**
     * Delete a role by its ID.
     *
     * @param id - the ID of the role to delete
     * @return true if the role was deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean deleteRoleById(Long id) {
        validateIdRole(id);
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Retrieve a role by its ID.
     *
     * @param id - the ID of the role to retrieve
     * @return RoleResponseDto
     * @throws IdInvalidException if the role ID is invalid or not found
     */
    @Override
    public RoleResponseDto getRoleById(Long id) {
        validateIdRole(id);
        return roleMapper.toDto(roleRepository.findById(id).orElseThrow(() -> new RoleIdNotFoundException("Role ID not found")));
    }

    /**
     * Retrieve all roles.
     *
     * @return a set of RoleResponseDto
     */
    @Override
    public Set<RoleResponseDto> getRoles() {
        return roleMapper.toSet(roleRepository.findAll());
    }

    /**
     * Validate the given role ID.
     *
     * @param id the role ID to validate
     * @throws IdInvalidException if the ID is null or less than or equal to zero
     *
     */
    @Override
    public void validateIdRole(Long id) {
        if (id == null || id <= 0) {
            throw new IdInvalidException("Invalid role ID");
        }
    }

    /**
     * * Validate the given Role object.
     *
     * @param roleRequestDto the Role object to validate
     * @throws RoleInvalidException            if the role is null
     * @throws RoleNameInvalidException        if the role name is null or empty
     * @throws RoleDescriptionInvalidException if the role description is null or empty
     *
     */
    @Override
    public void validateRole(RoleRequestDto roleRequestDto) {
        if (roleRequestDto == null) throw new RoleInvalidException("Invalid role update");
        if (roleRequestDto.getRoleName() == null || roleRequestDto.getRoleName().trim().isEmpty() || roleRequestDto.getRoleName().isEmpty())
            throw new RoleNameInvalidException("Invalid role name");
        if (roleRequestDto.getDescription() == null || roleRequestDto.getDescription().isEmpty() || roleRequestDto.getDescription().trim().isEmpty())
            throw new RoleDescriptionInvalidException("Invalid role description");
    }

    /**
     * Validate the given RoleUpdateDto object.
     *
     * @param roleUpdateDto the RoleUpdateDto object to validate
     * @throws IdInvalidException              if the role ID is null or less than or equal to zero
     * @throws RoleNameInvalidException        if the role name is null or empty
     * @throws RoleDescriptionInvalidException if the role description is null or empty
     *
     */
    @Override
    public void validateRoleUpdate(RoleUpdateDto roleUpdateDto) {
        if (roleUpdateDto.getRoleIdUpdate() == null || roleUpdateDto.getRoleIdUpdate() <= 0) {
            throw new IdInvalidException("Invalid role ID");
        }
        if (roleUpdateDto.getRoleNameUpdate() == null)
            throw new RoleNameInvalidException("Invalid role name");
        if (roleUpdateDto.getDescriptionUpdate() == null)
            throw new RoleDescriptionInvalidException("Invalid role description");
    }

    /**
     * Get a role by its name.
     *
     * @param roleName - the name of the role to retrieve
     * @return RoleResponseDto
     * @throws RoleInvalidException if the role name is not found
     */
    @Override
    public RoleResponseDto getRoleByName(String roleName) {
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName.toUpperCase());
        if (roleOptional.isPresent()) {
            return roleMapper.toDto(roleOptional.get());
        } else {
            throw new RoleNameNotFoundException("Role name not found");
        }
    }
}
