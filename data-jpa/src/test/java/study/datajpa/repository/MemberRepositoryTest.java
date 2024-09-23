package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  TeamRepository teamRepository;

  @Autowired
  EntityManager entityManager;

  @Test
  public void testMember() {
    Member member = new Member("memberA");
    Member savedMember = memberRepository.save(member);
    Member findMember = memberRepository.findById(savedMember.getId()).get();

    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

    assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
  }

  @Test
  public void basicCRUD() {
    Member member1 = new Member("member1");
    Member member2 = new Member("member2");
    memberRepository.save(member1);
    memberRepository.save(member2);

    //단건 조회 검증
    Member findMember1 = memberRepository.findById(member1.getId()).get();
    Member findMember2 = memberRepository.findById(member2.getId()).get();
    assertThat(findMember1).isEqualTo(member1);
    assertThat(findMember2).isEqualTo(member2);

    //리스트 조회 검증
    List<Member> all = memberRepository.findAll();
    assertThat(all.size()).isEqualTo(2);

    //카운트 검증
    long count = memberRepository.count();
    assertThat(count).isEqualTo(2);

    //삭제 검증
    memberRepository.delete(findMember1);
    memberRepository.delete(findMember2);

    long deletedCount = memberRepository.count();
    assertThat(deletedCount).isEqualTo(0);
  }

  @Test
  public void findByUsernameAndAgeGreaterThen() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

    assertThat(result.get(0).getUsername()).isEqualTo("AAA");
    assertThat(result.get(0).getAge()).isEqualTo(20);
    assertThat(result.size()).isEqualTo(1);
  }

  @Test
  public void testNamedQuery() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByUsername("AAA");
    Member findMember = result.get(0);
    assertThat(findMember).isEqualTo(m1);
  }

  @Test
  public void testQuery() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findUser("AAA",10);
    assertThat(result.get(0)).isEqualTo(m1);
  }

  @Test
  public void findUsernameList() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<String> usernameList = memberRepository.findUsernameList();
    for (String username : usernameList) {
      System.out.println("username = " + username);
    }
  }

  @Test
  public void findMemberDto() {
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    teamRepository.save(teamA);
    teamRepository.save(teamB);

    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    m1.setTeam(teamA);
    m2.setTeam(teamB);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<MemberDto> memberDto = memberRepository.findMemberDto();
    for (MemberDto dto : memberDto) {
      System.out.println("dto = " + dto);
    }
  }

  @Test
  public void findByNames() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBBB"));
    for (Member member : result) {
      System.out.println("member = " + member);
    }
  }

  @Test
  public void returnType() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> findMemberList = memberRepository.findListByUsername("AAA"); // 결과 없을 시 빈컬렉션 반환
    Member findMember = memberRepository.findMemberByUsername("AAA"); // 결과 없을 시 null 반환, 결과가 2건 이상 예외 발생
    Optional<Member> findMemberOptional = memberRepository.findOptionalByUsername("AAA"); //결과가 2건 이상 예외 발생
  }

  //페이징 조건과 정렬 조건 설정
  @Test
  public void page() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    //when
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

    //then
    List<Member> content = page.getContent(); //조회된 데이터
    long totalElements = page.getTotalElements();

    for (Member member : content) {
      System.out.println("member = " + member);
    }
    System.out.println("totalElements = " + totalElements);

    assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
    assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
    assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
    assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
    assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
    assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
  }
  @Test
  public void bulkUpdate() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 19));
    memberRepository.save(new Member("member3", 20));
    memberRepository.save(new Member("member4", 21));
    memberRepository.save(new Member("member5", 40));

    //when
    int resultCount = memberRepository.bulkAgePlus(20);
//    entityManager.flush();
//    entityManager.clear();

    List<Member> findMembers = memberRepository.findByUsername("member5");
    Member member5 = findMembers.get(0);
    System.out.println("member5 = " + member5);

    //then
    assertThat(resultCount).isEqualTo(3);
  }


  @Test
  public void findMemberLazy() {
    //given
    //member1 -> teamA
    //member2 -> teamB

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    teamRepository.save(teamA);
    teamRepository.save(teamB);
    memberRepository.save(new Member("member1", 10, teamA));
    memberRepository.save(new Member("member2", 20, teamB));

    entityManager.flush();
    entityManager.clear();

    //when N + 1
    //select Member 1
    List<Member> members = memberRepository.findAll();

    //then
    for (Member member : members) {
      System.out.println("member.getUsername() = " + member.getUsername());
      System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
    }
  }

  @Test
  public void queryHint() {
    //given
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);
    entityManager.flush();
    entityManager.clear();

    //when
//    Member findMember = memberRepository.findById(member1.getId()).get();
    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");

    entityManager.flush();

  }

  @Test
  public void lock() {
    //given
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);
    entityManager.flush();
    entityManager.clear();

    //when
    List<Member> result = memberRepository.findLockByUsername("member1");
  }

  @Test
  public void callCustom() {
    List<Member> result = memberRepository.findMemberCustom();
  }
}
