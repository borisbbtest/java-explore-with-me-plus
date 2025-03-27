package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.create(userDto), HttpStatus.CREATED);
    }

    @GetMapping
    public List<UserDto> getAllUsers(
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size
    ) {
        return userService.getAll(from, size);
    }

    @GetMapping(params = "ids")
    public ResponseEntity<List<UserDto>> getUserById(@RequestParam Long ids) {
        return ResponseEntity.ok(userService.getById(ids));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}