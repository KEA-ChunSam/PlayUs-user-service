package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}
