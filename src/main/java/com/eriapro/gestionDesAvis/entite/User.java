package com.eriapro.gestionDesAvis.entite;

import java.util.Collection;

import java.util.Set;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.HashSet;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="users")
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	private String name;
	private String password;
	@Column(unique = true)
	private String email;
	private boolean actif;
	@OneToMany(mappedBy = "user" , fetch = FetchType.EAGER)
	@Builder.Default
	private Set<UserRole> userRoles = new HashSet<>();
	@OneToMany(mappedBy = "user")
	@Builder.Default
	private Set<Avis> avis = new HashSet<>();
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    Set<GrantedAuthority> authorities = new HashSet<>();
	    userRoles.forEach(userRole -> {
	        Role role = userRole.getRole();
	        if (role != null) {
	            // Ajout du rôle comme autorité Spring Security
	            authorities.add(
	                new SimpleGrantedAuthority(
	                    "ROLE_" + role.getName()
	                )
	            );
	            // Ajout des permissions liées au rôle
	            role.getPermissions()
	                .forEach(rolePermission -> {
	                    if (rolePermission.getPermission() != null) {
	                        authorities.add(
	                            new SimpleGrantedAuthority(
	                                rolePermission
	                                    .getPermission()
	                                    .getCode()
	                            )
	                        );
	                    }

	                });
	        }

	    });

	    return authorities;
	}
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}
	public String getPassword() {
		
		return password;
	}

	
	public boolean isAccountNonExpired() {
		return actif;
	}

	
	public boolean isAccountNonLocked() {
		return actif;
	}

	
	public boolean isCredentialsNonExpired() {
		return actif;
	}

	
	public boolean isEnabled() {
		return actif;
	}


}
