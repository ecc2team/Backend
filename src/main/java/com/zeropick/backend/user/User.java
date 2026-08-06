package com.zeropick.backend.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.zeropick.backend.user.AuthProvider;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 100)
    private String password; // 소셜 로그인 유저는 null

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public User(String email, String password, AuthProvider provider) {
        this.email = email;
        this.password = password;
        this.provider = provider;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
