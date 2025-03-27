package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    List<UserDto> getAll(int from, int size);

    List<UserDto> getById(Long id);

    void deleteUser(Long userId);
}
