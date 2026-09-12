package com.eriapro.gestionDesAvis.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eriapro.gestionDesAvis.DTO.userDTO.UserRequestDTO;
import com.eriapro.gestionDesAvis.entite.Role;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.entite.UserRole;
import com.eriapro.gestionDesAvis.entite.Validation;
import com.eriapro.gestionDesAvis.exception.InvalidEmailException;
import com.eriapro.gestionDesAvis.exception.InvalidPasswordException;
import com.eriapro.gestionDesAvis.exception.RoleNotFoundException;
import com.eriapro.gestionDesAvis.exception.UserAlreadyExistsException;
import com.eriapro.gestionDesAvis.exception.UserNotFoundException;
import com.eriapro.gestionDesAvis.exception.ValidationCodeExpiredException;
import com.eriapro.gestionDesAvis.exception.ValidationCodeNotFoundException;
import com.eriapro.gestionDesAvis.mapper.UserMapper;
import com.eriapro.gestionDesAvis.repository.RoleRepository;
import com.eriapro.gestionDesAvis.repository.UserRepository;
import com.eriapro.gestionDesAvis.repository.UserRoleRepository;
import com.eriapro.gestionDesAvis.repository.ValidationRepository;
import com.eriapro.gestionDesAvis.service.UserService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationRepository validationRepository;
    private final ValidationServiceImpl validationServiceImpl;



    @Override
    @Transactional
    public void inscription(UserRequestDTO userRequestDTO) {


        User user = UserMapper.toEntity(userRequestDTO);



        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";


        String passwordRegex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!?.*_])[A-Za-z\\d@#$%^&+=!?.*_]{8,}$";



        if (!user.getEmail().matches(emailRegex)) {

            throw new InvalidEmailException(
                    "Adresse e-mail invalide"
            );
        }



        if (!user.getPassword().matches(passwordRegex)) {

            throw new InvalidPasswordException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
            );
        }





        Optional<User> userExist =
                userRepository.findByEmailWithAuthorities(user.getEmail());



        if (userExist.isPresent()) {


            if(userExist.get().isActif()) {

                throw new UserAlreadyExistsException(
                        "Utilisateur existe déjà"
                );

            } else {


                validationServiceImpl.saveValidation(
                        userExist.get()
                );

                return;
            }

        }




        /*
         * Recherche du rôle par défaut
         */
        Role roleClient =
                roleRepository.findByName("CLIENT")
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Le rôle CLIENT n'existe pas"
                        )
                );



        /*
         * Encodage du mot de passe
         */
        String passEncode =
                passwordEncoder.encode(
                        user.getPassword()
                );


        user.setPassword(passEncode);



        /*
         * Création utilisateur
         */
        User userSave =
                userRepository.save(user);




        /*
         * Association User <-> Role
         */
        UserRole userRole =
                UserRole.builder()
                .user(userSave)
                .role(roleClient)
                .createdAt(Instant.now())
                .build();



        userRoleRepository.save(userRole);




        /*
         * Création validation compte
         */
        validationServiceImpl.saveValidation(userSave);


    }




    @Override
    public void activation(Map<String, String> codeActivation) {


        Validation validation =
                validationServiceImpl.lireLeCode(
                        codeActivation.get("code")
                );



        if (Instant.now().isAfter(validation.getExpiration())) {

            throw new ValidationCodeExpiredException(
                    "Votre code est expiré"
            );
        }



        User userActive =
                userRepository.findById(
                        validation.getUser().getId()
                )
                .orElseThrow(() ->
                        new ValidationCodeNotFoundException(
                                "Utilisateur inconnu"
                        )
                );



        userActive.setActif(true);


        validation.setActivation(
                Instant.now()
        );



        validationRepository.save(validation);


        userRepository.save(userActive);

    }





    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {


    	 User user =  userRepository.findByEmailWithAuthorities(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Veuillez vérifier vos identifiants"
                        )
                );
          return user;
    }





    public void updatePassword(Map<String, String> param) {


        User user =
                (User) this.loadUserByUsername(
                        param.get("email")
                );


        validationServiceImpl.saveValidation(user);

    }





    public void newPassWord(Map<String, String> param) {


        User user =
                (User) this.loadUserByUsername(
                        param.get("email")
                );



        Validation validation =
                validationServiceImpl.lireLeCode(
                        param.get("code")
                );



        if (Instant.now().isAfter(validation.getExpiration())) {

            throw new RuntimeException(
                    "Votre code est expiré"
            );
        }




        if(validation.getUser().getEmail()
                .equals(user.getEmail())) {


            String passEncode =
                    passwordEncoder.encode(
                            param.get("password")
                    );


            user.setPassword(passEncode);


            userRepository.save(user);

        }

    }

}