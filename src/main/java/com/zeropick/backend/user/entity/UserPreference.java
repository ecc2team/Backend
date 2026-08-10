package com.zeropick.backend.user.entity;

import com.zeropick.backend.user.PreferenceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_type", nullable=false, length=30)
    private PreferenceType preferenceType;

    @Column(nullable = false, length = 50)
    private String value;

    @Builder
    public UserPreference(User user, PreferenceType type, String value) {
        this.user = user;
        this.preferenceType = type;
        this.value = value;
    }
}

