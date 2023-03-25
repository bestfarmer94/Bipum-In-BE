package com.sparta.bipuminbe.supply.dto;

import com.sparta.bipuminbe.common.entity.Requests;
import com.sparta.bipuminbe.common.entity.Supply;
import com.sparta.bipuminbe.common.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class SupplyHistoryResponseDto {

    private LocalDateTime modifiedAt;
//    private String username;
    private String empName;
    private String deptName;

    private String content;

    public SupplyHistoryResponseDto(Requests request){
        Supply supply = request.getSupply();
        this.modifiedAt = supply.getModifiedAt();
//        this.username = supply.getUser().getUsername();
        this.empName = supply.getUser() == null ? null : supply.getUser().getEmpName();
        this.deptName = supply.getUser() == null ? null : supply.getUser().getDepartment().getDeptName();
        this.content = request.getRequestType()== RequestType.SUPPLY ? "사용" : "반납";
    }
}
