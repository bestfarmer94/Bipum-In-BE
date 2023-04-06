package com.sparta.bipuminbe.common.entity;

import com.sparta.bipuminbe.common.entity.Requests;
import com.sparta.bipuminbe.common.entity.TimeStamped;
import com.sparta.bipuminbe.common.entity.User;
import com.sparta.bipuminbe.common.enums.AcceptResult;
import com.sparta.bipuminbe.common.enums.NotificationType;
import com.sparta.bipuminbe.common.enums.RequestStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Notification extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    //알림의 내용
    @Column(nullable = false)
    private String content;

    //알림을 읽었는지 여부 체크
    @Column(nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private AcceptResult acceptResult;

    //알림 종류
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private NotificationType notificationType;

    //회원 정보에 대한 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requests_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Requests request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requests_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Supply supply;

    @Builder
    public Notification(User sender, User receiver, String content,
                        Boolean isRead, Requests request, NotificationType notificationType,
                        AcceptResult acceptResult, Supply supply) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = isRead;
        this.request = request;
        this.notificationType = notificationType;
        this.acceptResult = acceptResult;
        this.supply = supply;
    }

    public void read(){
        this.isRead = true;
    }
}
