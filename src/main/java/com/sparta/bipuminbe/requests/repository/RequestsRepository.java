package com.sparta.bipuminbe.requests.repository;

import com.sparta.bipuminbe.common.entity.Requests;
import com.sparta.bipuminbe.common.enums.RequestStatus;
import com.sparta.bipuminbe.common.enums.RequestType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RequestsRepository extends JpaRepository<Requests, Long> {
    List<Requests> findByRequestTypeInAndRequestStatusIn(Set<RequestType> requestTypeQuery, Set<RequestStatus> requestStatusQuery, Pageable pageable);
}
