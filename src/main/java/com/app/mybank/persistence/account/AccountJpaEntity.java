package com.app.mybank.persistence.account;

import com.app.mybank.persistence.common.MoneyEmbeddable;
import com.app.mybank.persistence.user.UserJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AccountJpaEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_user"))
    private UserJpaEntity owner;

    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatusJpa status;

    @Column(name = "daily_limit", precision = 19, scale = 4, nullable = false)
    private BigDecimal dailyLimit;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

