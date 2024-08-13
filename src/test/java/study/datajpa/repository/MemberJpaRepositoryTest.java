package study.datajpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.entity.Member;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("회원 저장 및 조회")
    @Test
    void save() {
        // given
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);

        // when
        Member findMember = memberJpaRepository.find(member.getId());

        // then
        assertThat(savedMember.getId()).isEqualTo(member.getId());
        assertThat(savedMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(savedMember).isEqualTo(member);
    }

    @DisplayName("회원 [등록, 조회, 수정, 삭제]")
    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberJpaRepository.findAll();
        assertThat(members).hasSize(2)
            .extracting("username")
            .contains("member1", "member2");

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @DisplayName("주어진 회원 이름과 나이에 대해 회원의 이름이 같고 나이가 더 많은 회원 조회")
    @Test
    void findByUsernameAndAgeGreaterThan() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberA", 15);
        Member memberC = new Member("memberB", 20);

        memberJpaRepository.save(memberA);
        memberJpaRepository.save(memberB);
        memberJpaRepository.save(memberC);

        // when
        List<Member> findMembers = memberJpaRepository.findByUsernameAndAgeGreaterThan("memberA", 10);

        // then
        assertThat(findMembers).hasSize(1)
            .extracting("username", "age")
            .contains(
                tuple("memberA", 15)
            );
    }

    @DisplayName("회원 조회 페이징")
    @Test
    void paging() {
        // given
        Member memberA = new Member("memberA", 20);
        Member memberB = new Member("memberB", 20);
        Member memberC = new Member("memberC", 20);
        Member memberD = new Member("memberD", 20);
        Member memberE = new Member("memberE", 20);

        memberJpaRepository.save(memberA);
        memberJpaRepository.save(memberB);
        memberJpaRepository.save(memberC);
        memberJpaRepository.save(memberD);
        memberJpaRepository.save(memberE);

        int age = 20;
        int offset = 0;
        int limit = 3;

        // when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        // then
        assertThat(members).hasSize(3)
            .extracting("username")
            .contains("memberC", "memberD", "memberE");

        assertThat(totalCount).isEqualTo(5);
    }

    @DisplayName("특정 나이보다 나이가 많은 회원의 나이를 1 증가")
    @Test
    void bulkAgePlus() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 19);
        Member memberC = new Member("memberC", 20);
        Member memberD = new Member("memberD", 21);
        Member memberE = new Member("memberE", 40);

        memberJpaRepository.save(memberA);
        memberJpaRepository.save(memberB);
        memberJpaRepository.save(memberC);
        memberJpaRepository.save(memberD);
        memberJpaRepository.save(memberE);

        // when
        int resultCount = memberJpaRepository.bulkAgePlus(20);
        em.clear();

        Member findMemberE = memberJpaRepository.findByUsername("memberE").get(0);
        System.out.println("memberE = " + findMemberE);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

}