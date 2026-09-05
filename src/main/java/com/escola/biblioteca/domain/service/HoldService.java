package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Hold;
import com.escola.biblioteca.domain.model.HoldStatus;
import com.escola.biblioteca.domain.model.OutboxEvent;
import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.repository.HoldRepository;
import com.escola.biblioteca.domain.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final HoldRepository holdRepository;
    private final PatronService patronService;
    private final OutboxEventRepository outboxEventRepository;

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
        hold.setPlacedAt(Instant.now());
        hold.marcarNovo();

        Hold saved = holdRepository.save(hold);
        publishEvent(saved.getId(), "HoldPlaced", saved);
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
        hold.setCancelledAt(Instant.now());
        holdRepository.save(hold);

        // Reorder positions for remaining waiting holds
        reorderPositions(hold.getWorkId(), hold.getLibraryId());
        publishEvent(hold.getId(), "HoldCancelled", hold);
    }

    @Transactional
    public Hold fulfillHold(UUID holdId) {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.hold"));

        if (hold.getStatus() != HoldStatus.READY) {
            throw new BusinessException("error.hold.not.ready");
        }

        hold.setStatus(HoldStatus.FULFILLED);
        hold.setFulfilledAt(Instant.now());
        holdRepository.save(hold);

        // Trigger next in queue
        Optional<Hold> next = holdRepository.findNextWaitingByWorkIdAndLibraryId(hold.getWorkId(), hold.getLibraryId());
        next.ifPresent(this::notifyReady);

        publishEvent(hold.getId(), "HoldFulfilled", hold);
        return hold;
    }

//    @Transactional
//    public void expireHolds() {
//        List<Hold> expired = holdRepository.findReadyExpired();
//        for (Hold hold : expired) {
//            hold.setStatus(HoldStatus.EXPIRED);
//            hold.setCancelledAt(Instant.now());
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
        hold.setReadyAt(Instant.now());
        hold.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));
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
        event.marcarNovo();
        outboxEventRepository.save(event);
    }
}