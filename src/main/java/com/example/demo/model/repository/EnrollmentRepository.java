package com.example.demo.model.repository;

import com.example.demo.model.domain.Enrollment;
import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByMemberAndPost(Member member, Post post);

    boolean existsByMember(Member member);

    boolean existsByPost(Post post);

    List<Enrollment> findByMember_UsernameOrderByIdDesc(String username);
}
