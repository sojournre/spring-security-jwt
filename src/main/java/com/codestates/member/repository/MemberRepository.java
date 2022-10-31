package com.codestates.member.repository;

import com.codestates.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUid(String uid);

    @Query("UPDATE Member m set m.failedAttempt = ?1 where m.uid = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String uid);
}
