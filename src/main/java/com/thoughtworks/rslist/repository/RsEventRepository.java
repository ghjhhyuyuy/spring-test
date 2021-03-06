package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
  @Query(value = "select * from rs_event order by rank", nativeQuery = true)
  List<RsEventDto> findAll();

  @Transactional
  void deleteAllByUserId(int userId);
}
