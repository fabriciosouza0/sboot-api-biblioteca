package com.escola.biblioteca.domain.circulation.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.circulation.model.Hold;
import com.escola.biblioteca.domain.circulation.model.enums.HoldStatus;
import com.escola.biblioteca.domain.circulation.policy.LoanPolicy;
import com.escola.biblioteca.domain.outbox.model.OutboxEvent;
import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.domain.circulation.repository.HoldRepository;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final HoldRepository holdRepository;
    private final PatronService patronService;
    private final OutboxEventRepository outboxEventRepository;
    private final DashboardPublisher dashboardPublisher;

    @Transactional
    public Hold placeHold(UUID patronId, UUID workId, UUID libraryId) {
        Patron patron = patronService.findById(patronId);
        if (patronService.isBlocked(patron)) {
            throw new BusinessException("error.patron.blocked");
        }

        LoanPolicy policy = patronService.getLimits(patron);
        long activeHolds = holdRepository.findByPatronId(patronId).stream()
                .filter(h -> h.getStatus() == HoldStatus.WAITING || h.getStatus() == HoldStatus.READY)
                .count();
        if (activeHolds >= policy.getHoldLimit()) {
            throw new BusinessException("error.patron.hold.limit.exceeded", policy.getHoldLimit());
        }

        // Calculate position in queue
        List<Hold> waitingHolds = holdRepository.findWaitingByWorkIdAndLibraryId(workId, libraryId);
        int position = waitingHolds.size() + 1;

        Hold hold = new Hold();
        hold.setPatronId(patronId);
        hold.setWorkId(workId);
        hold.setLibraryId(libraryId);
        hold.setStatus(HoldStatus.WAITING);
        hold.setPosition(position);
        hold.setPlacedAt(OffsetDateTime.now());
        hold.marcarNovo();

        Hold saved = holdRepository.save(hold);
        publishEvent(saved.getId(), "HoldPlaced", saved);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void cancelHold(UUID holdId) {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.hold"));

        if (hold.getStatus() == HoldStatus.FULFILLED || hold.getStatus() == HoldStatus.CANCELLED) {
            throw new BusinessException("error.hold.cannot.cancel");
        }

        hold.setStatus(HoldStatus.CANCELLED);
        hold.setCancelledAt(OffsetDateTime.now());
        holdRepository.save(hold);

        // Reorder positions for remaining waiting holds
        reorderPositions(hold.getWorkId(), hold.getLibraryId());
        publishEvent(hold.getId(), "HoldCancelled", hold);
        dashboardPublisher.dadosAlterados();
    }

    @Transactional
    public Hold fulfillHold(UUID holdId) {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.hold"));

        if (hold.getStatus() != HoldStatus.READY) {
            throw new BusinessException("error.hold.not.ready");
        }

        hold.setStatus(HoldStatus.FULFILLED);
        hold.setFulfilledAt(OffsetDateTime.now());
        holdRepository.save(hold);

        // Trigger next in queue
        Optional<Hold> next = holdRepository.findNextWaitingByWorkIdAndLibraryId(hold.getWorkId(), hold.getLibraryId());
        next.ifPresent(this::notifyReady);

        publishEvent(hold.getId(), "HoldFulfilled", hold);
        dashboardPublisher.dadosAlterados();
        return hold;
    }

    public List<Hold> findAll() {
        var all = new ArrayList<Hold>();
        holdRepository.findAll().forEach(all::add);
        return all;
    }

//    @Transactional
//    public void expireHolds() {
//        List<Hold> expired = holdRepository.findReadyExpired();
//        for (Hold hold : expired) {
//            hold.setStatus(HoldStatus.EXPIRED);
//            hold.setCancelledAt(OffsetDateTime.now());
//            holdRepository.save(hold);
//            reorderPositions(hold.getWorkId(), hold.getLibraryId());
//            publishEvent(hold.getId(), "HoldExpired", hold);
//
//            Optional<Hold> next = holdRepository.findNextWaitingByWorkIdAndLibraryId(hold.getWorkId(), hold.getLibraryId());
//            next.ifPresent(this::notifyReady);
//        }
//    }

    private void notifyReady(Hold hold) {
        hold.setStatus(HoldStatus.READY);
        hold.setReadyAt(OffsetDateTime.now());
        hold.setExpiresAt(OffsetDateTime.now().plus(48, ChronoUnit.HOURS));
        holdRepository.save(hold);
        publishEvent(hold.getId(), "HoldReady", hold);
    }

    private void reorderPositions(UUID workId, UUID libraryId) {
        List<Hold> waiting = holdRepository.findWaitingByWorkIdAndLibraryId(workId, libraryId);
        for (int i = 0; i < waiting.size(); i++) {
            waiting.get(i).setPosition(i + 1);
            holdRepository.save(waiting.get(i));
        }
    }

    private void publishEvent(UUID aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Hold");
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(com.escola.biblioteca.util.JsonUtil.toJson(payload));
        event.setCreatedAt(java.time.OffsetDateTime.now());
        event.marcarNovo();
        outboxEventRepository.save(event);
    }
}