package com.konnectnet.core.user.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.konnectnet.core.auth.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String bio;
    private String profilePictureUrl;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private AppUser user;
//
//    @OneToMany(mappedBy = "userDetail", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Post> posts = new ArrayList<>();
}