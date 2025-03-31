package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email уже существует: " + userDto.getEmail());
        }

        User user = userRepository.save(UserMapper.toEntity(userDto));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return userRepository.findAll(pageable)
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getById(Long id) {
        return userRepository.findById(id).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
