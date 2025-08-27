package com.digital.wallet.adapter.repositories.impl;

import com.digital.wallet.adapter.converters.UserMapper;
import com.digital.wallet.adapter.entities.UserEntity;
import com.digital.wallet.adapter.repositories.UserJpaRepository;
import com.digital.wallet.core.domain.User;
import com.digital.wallet.core.ports.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper userMapper) {
        this.jpaRepository = jpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaRepository.findById(userId).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public void update(User user) {
        jpaRepository.save(userMapper.toEntity(user));
    }

    @Override
    public void delete(UUID userId) {
        jpaRepository.deleteById(userId);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(userMapper::toDomain).toList();
    }
}
