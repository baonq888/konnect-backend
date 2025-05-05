package com.konnectnet.core.post.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.post.enums.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate = new Date();

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<AppUser> likedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Shared Post is a Reference to the Original Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    @JsonBackReference
    private Post originalPost;


    @OneToMany(mappedBy = "originalPost", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Post> sharedPosts = new ArrayList<>();

    public Post(String content, Visibility visibility) {
        this.content = content;
        this.visibility = visibility;
    }

}