package com.studio.service;

import com.studio.exception.BusinessException;
import com.studio.exception.EntityNotFoundException;
import com.studio.model.Client;
import com.studio.repository.ClientRepository;

import java.util.List;

public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public Client create(Client client) {
        validate(client);
        repository.findByEmail(client.getEmail()).ifPresent(c -> {
            throw new BusinessException("Клиент с email '" + client.getEmail() + "' уже существует");
        });
        return repository.save(client);
    }

    public Client getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с id=" + id + " не найден"));
    }

    public List<Client> getAll() {
        return repository.findAll();
    }

    public void update(Client client) {
        validate(client);
        if (!repository.existsById(client.getId())) {
            throw new EntityNotFoundException("Клиент с id=" + client.getId() + " не найден");
        }
        repository.update(client);
    }

    public void delete(int id) {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException("Клиент с id=" + id + " не найден");
        }
    }

    public boolean exists(int id) {
        return repository.existsById(id);
    }

    private void validate(Client client) {
        if (client == null) {
            throw new BusinessException("Клиент не может быть null");
        }
        if (client.getFullName() == null || client.getFullName().isBlank()) {
            throw new BusinessException("ФИО клиента обязательно");
        }
        if (client.getFullName().length() < 2) {
            throw new BusinessException("ФИО должно содержать минимум 2 символа");
        }
        if (client.getEmail() == null
                || !client.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new BusinessException("Некорректный email клиента");
        }
        if (client.getPhone() == null
                || !client.getPhone().matches("^\\+7\\d{10}$")) {
            throw new BusinessException("Телефон должен иметь формат +7XXXXXXXXXX");
        }
        if (client.getRole() == null) {
            throw new BusinessException("Роль клиента обязательна");
        }
    }
}
