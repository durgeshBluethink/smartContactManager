package io.project.smartcontactmanager.repository;

import io.project.smartcontactmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {

}
