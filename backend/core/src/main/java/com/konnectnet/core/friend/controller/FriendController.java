package com.konnectnet.core.friend.controller;

import com.konnectnet.core.friend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@Tag(name = "Friend", description = "Endpoints to manage friend relationships")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(
            summary = "Add a friend",
            description = "Adds another user as a friend"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friend added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or friend ID")
    })
    @PostMapping
    public ResponseEntity<String> addFriend(
            @RequestParam UUID userId,
            @RequestParam UUID friendId) {
        friendService.addFriend(userId, friendId);
        return ResponseEntity.ok("Friend added successfully");
    }

    @Operation(
            summary = "Remove a friend",
            description = "Removes an existing friend relationship"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friend removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or friend ID")
    })
    @DeleteMapping
    public ResponseEntity<String> removeFriend(
            @RequestParam UUID userId,
            @RequestParam UUID friendId) {
        friendService.unfriend(userId, friendId);
        return ResponseEntity.ok("Friend removed successfully");
    }

    @Operation(
            summary = "Get user's friends",
            description = "Retrieves a paginated list of a user's friends"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friends retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    @GetMapping
    public ResponseEntity<?> getFriends(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(friendService.getFriends(userId, pageable));
    }
}