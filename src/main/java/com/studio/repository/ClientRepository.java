package com.studio.repository;

import com.studio.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(int id);
    Optional<Client> findByEmail(String email);
    List<Client> findAll();
    boolean update(Client client);
    boolean deleteById(int id);
    boolean existsById(int id);
}