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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }
  @Transactional
  public void buy(Trade trade, int id) throws Exception {
    Optional<RsEventDto> optionalRsEventDto = rsEventRepository.findById(id);
    RsEventDto rsEventDto = new RsEventDto();
    if(optionalRsEventDto.isPresent()){
      rsEventDto = optionalRsEventDto.get();
    }else {
      throw new Exception("wrong rsEventId");
    }

    TradeDto tradeDto = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEventDto(rsEventDto).build();
    TradeDto originTradeDto = tradeRepository.findByRank(tradeDto.getRank());
    if(originTradeDto == null){
      tradeRepository.save(tradeDto);
      rsEventDto.setRank(tradeDto.getRank());
      rsEventRepository.save(rsEventDto);
    }else if(originTradeDto.getAmount() < tradeDto.getAmount()){
      tradeRepository.save(tradeDto);
      tradeRepository.deleteById(originTradeDto.getId());
      rsEventRepository.deleteById(originTradeDto.getRsEventDto().getId());
      rsEventDto.setRank(tradeDto.getRank());
      rsEventRepository.save(rsEventDto);
    }else {
      throw new Exception("amount less than origin");
    }
  }
}
