package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock
  TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository,tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }
  @Test
  void shouldBuyRsEventWhenNoCompetition() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(10)
                    .phone("188888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("idolice")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").id(1).rank(1).user(userDto).build();
    Trade trade = new Trade(400,1);
    TradeDto tradeDto = TradeDto.builder().rsEventDto(rsEventDto).amount(400).rank(1).build();
    when(rsEventRepository.findById(1)).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(trade.getRank())).thenReturn(null);
    rsService.buy(trade,1);
    verify(tradeRepository).save(tradeDto);
    verify(rsEventRepository).save(rsEventDto);
  }
  @Test
  void shouldBuyRsEventWhenAmountBetter() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(10)
                    .phone("188888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("idolice")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(1).user(userDto).build();
    RsEventDto rsEventDtoNew =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(2).user(userDto).build();
    Trade trade = new Trade(400,1);
    TradeDto tradeDto = TradeDto.builder().rsEventDto(rsEventDto).amount(200).rank(1).build();
    TradeDto tradeDtoNew = TradeDto.builder().rsEventDto(rsEventDtoNew).amount(400).rank(1).build();
    when(rsEventRepository.findById(2)).thenReturn(Optional.of(rsEventDtoNew));
    when(tradeRepository.findByRank(trade.getRank())).thenReturn(tradeDto);
    rsService.buy(trade,2);
    verify(tradeRepository).save(tradeDtoNew);
    verify(rsEventRepository).save(rsEventDtoNew);
    verify(tradeRepository).deleteById(tradeDto.getId());
    verify(rsEventRepository).deleteById(rsEventDto.getId());
  }
  @Test
  void shouldReturn400WhenAmountFewer() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(10)
                    .phone("188888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("idolice")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(1).user(userDto).build();
    RsEventDto rsEventDtoNew =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(2).user(userDto).build();
    Trade trade = new Trade(400,1);
    TradeDto tradeDto = TradeDto.builder().rsEventDto(rsEventDto).amount(600).rank(1).build();
    TradeDto tradeDtoNew = TradeDto.builder().rsEventDto(rsEventDtoNew).amount(400).rank(1).build();
    when(rsEventRepository.findById(2)).thenReturn(Optional.of(rsEventDtoNew));
    when(tradeRepository.findByRank(trade.getRank())).thenReturn(tradeDto);
    assertThrows(
            Exception.class,
            () -> {
              rsService.buy(trade,2);
            });
  }
  @Test
  void shouldReturn400WhenRsEventIdWrong() throws Exception {
    Trade trade = new Trade(400,1);
    when(rsEventRepository.findById(2)).thenReturn(Optional.empty());
    assertThrows(
            Exception.class,
            () -> {
              rsService.buy(trade,2);
            });
  }
}
