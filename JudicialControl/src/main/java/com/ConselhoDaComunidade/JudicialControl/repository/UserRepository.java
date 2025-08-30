package com.ConselhoDaComunidade.JudicialControl.repository;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findById(Long id);

    @Query("SELECT u FROM User u WHERE trim(u.cpf) = :cpf AND trim(u.senha) = :senha")
    Optional<User> login(@Param("cpf") String cpf, @Param("senha") String senha);
}
