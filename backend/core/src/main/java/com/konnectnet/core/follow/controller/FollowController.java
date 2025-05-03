package com.konnectnet.core.follow.controller;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/follow")
@Tag(name = "Follow", description = "Endpoints to manage user follow relationships")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Follow a user", description = "Follow another user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User followed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user IDs")
    })
    @PostMapping
    public ResponseEntity<String> followUser(
            @RequestParam UUID followerId,
            @RequestParam UUID followeeId) {
        followService.followUser(followerId, followeeId);
        return ResponseEntity.ok("User followed successfully");
    }

    @Operation(summary = "Unfollow a user", description = "Unfollow a previously followed user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User unfollowed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user IDs")
    })
    @DeleteMapping
    public ResponseEntity<String> unfollowUser(
            @RequestParam UUID followerId,
            @RequestParam UUID followeeId) {
        followService.unfollowUser(followerId, followeeId);
        return ResponseEntity.ok("User unfollowed successfully");
    }

    @Operation(summary = "Get followers", description = "Retrieve list of users who follow the specified user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Followers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    @GetMapping("/followers")
    public ResponseEntity<List<AppUser>> getFollowers(@RequestParam UUID userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @Operation(summary = "Get following", description = "Retrieve list of users the specified user is following")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Following list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    @GetMapping("/following")
    public ResponseEntity<List<AppUser>> getFollowing(@RequestParam UUID userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }
}