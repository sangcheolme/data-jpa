package study.datajpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("회원 저장 및 조회")
    @Test
    void save() {
        // given
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        // when
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @DisplayName("회원 [등록, 조회, 수정, 삭제]")
    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(2)
            .extracting("username")
            .contains("member1", "member2");

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @DisplayName("주어진 회원 이름과 나이에 대해 회원의 이름이 같고 나이가 더 많은 회원 조회")
    @Test
    void findByUsernameAndAgeGreaterThan() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberA", 15);
        Member memberC = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);

        // when
        List<Member> findMembers = memberRepository.findByUsernameAndAgeGreaterThan("memberA", 10);

        // then
        assertThat(findMembers).hasSize(1)
            .extracting("username", "age")
            .contains(
                tuple("memberA", 15)
            );
    }

    @DisplayName("이름과 나이가 같은 회원 조회")
    @Test
    void findMember() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberA", 15);
        Member memberC = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);

        // when
        List<Member> findMembers = memberRepository.findMember("memberA", 15);

        // then
        assertThat(findMembers).hasSize(1)
            .extracting("username", "age")
            .contains(
                tuple("memberA", 15)
            );
    }

    @DisplayName("회원 이름 리스트 조회")
    @Test
    void findUsernameList() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberA", 15);
        Member memberC = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);

        // when
        List<String> usernameList = memberRepository.findUsernameList();

        // then
        assertThat(usernameList).hasSize(3)
            .contains("memberA", "memberA", "memberB");
    }

    @DisplayName("회원 Dto로 조회")
    @Test
    void findMemberDto() {
        // given
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        teamRepository.save(team1);
        teamRepository.save(team2);

        Member memberA = new Member("memberA", 10, team1);
        Member memberB = new Member("memberA", 15, team1);
        Member memberC = new Member("memberB", 20, team2);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);

        // when
        List<MemberDto> memberDto = memberRepository.findMemberDto();

        // then
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @DisplayName("회원 이름 리스트로 회원 조회")
    @Test
    void findByNames() {
        // given
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        Member memberC = new Member("memberC", 25);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);

        // when
        List<Member> findMembers = memberRepository.findByNames(List.of("memberA", "memberB"));

        // then
        assertThat(findMembers).hasSize(2)
            .extracting("username")
            .contains("memberA", "memberB");
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

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);
        memberRepository.save(memberD);
        memberRepository.save(memberE);

        int age = 20;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // Page Entity -> Dto 변환
        Page<MemberDto> memberDtos = page.map(MemberDto::new);
        for (MemberDto memberDto : memberDtos) {
            System.out.println("memberDto = " + memberDto);
        }

        List<Member> members = page.getContent();

        // then
        assertThat(members).hasSize(3)
            .extracting("username")
            .contains("memberC", "memberD", "memberE");

        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 수
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지 있음?
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

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);
        memberRepository.save(memberD);
        memberRepository.save(memberE);

        // when
        int resultCount = memberRepository.bulkAgePlus(20);
        // em.clear();

        Member findMemberE = memberRepository.findByUsername("memberE").get(0);
        System.out.println("memberE = " + findMemberE);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @DisplayName("지연로딩 회원 조회")
    @Test
    void findMemberLazy() {
        // given
        // member1 -> teamA
        // member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        // select Member
        // N + 1 문제
        List<Member> members = memberRepository.findAll();
        // List<Member> members = memberRepository.findMemberFetchJoin();
        // List<Member> members = memberRepository.findMemberEntityGraph();
        // List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        // then
        for (Member member : members) {
            System.out.println("member.username = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @DisplayName("JPA query hint")
    @Test
    void queryHint() {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        // then
        em.flush();
    }

    @DisplayName("JPA Lock")
    @Test
    void lock() {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findLockByUsername("member1").get(0);
        findMember.setUsername("member2");
    }

    @DisplayName("callCustom")
    @Test
    void callCustom() {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        List<Member> result = memberRepository.findMemberCustom();
        System.out.println("result = " + result);
    }

}