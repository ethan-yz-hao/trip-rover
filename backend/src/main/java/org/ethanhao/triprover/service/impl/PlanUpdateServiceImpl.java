package org.ethanhao.triprover.service.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class PlanUpdateServiceImpl implements PlanUpdateService {

    private final PlanRepository planRepository;

    @Autowired
    public PlanUpdateServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public Long updatePlanWithMessage(Long planId, PlanUpdateMessage updateMessage) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        // Validate version
        if (!Objects.equals(plan.getVersion(), updateMessage.getVersion())) {
            throw new OptimisticLockException("Version mismatch. Current version: " + plan.getVersion());
        }

        // Apply the update message to the plan
        switch (updateMessage.getAction()) {
            case REORDER:
                reorderPlaces(plan, updateMessage);
                break;
            case ADD:
                addPlace(plan, updateMessage);
                break;
            case REMOVE:
                removePlace(plan, updateMessage);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + updateMessage.getAction());
        }

        // Increment the version
        plan.setVersion(plan.getVersion() + 1);

        // Save the updated plan
        Plan savedPlan = planRepository.save(plan);

        return savedPlan.getVersion();
    }

    // Private helper methods

    private void reorderPlaces(Plan plan, PlanUpdateMessage updateMessage) {
        String placeId = updateMessage.getPlaceId();
        String targetPlaceId = updateMessage.getTargetPlaceId();

        PlanPlace placeToMove = plan.getPlaces().stream()
                .filter(place -> place.getPlaceId().equals(placeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid placeId for reorder: " + placeId));
        plan.getPlaces().remove(placeToMove);

        if (targetPlaceId != null && !targetPlaceId.isEmpty()) {
            int targetIndex = plan.getPlaces().stream()
                    .map(PlanPlace::getPlaceId)
                    .toList()
                    .indexOf(targetPlaceId);

            // Adjust target index (insert after the targetIndex) if moving down
            if (targetIndex >= placeToMove.getSequenceNumber()) {
                targetIndex++;
            }

            if (targetIndex == -1) {
                throw new IllegalArgumentException("Invalid targetPlaceId for reorder: " + targetPlaceId);
            }

            plan.getPlaces().add(targetIndex, placeToMove);
        } else {
            // If targetPlaceId is not provided, place at the end
            plan.getPlaces().add(placeToMove);
        }

        // Update sequence numbers
        for (int i = 0; i < plan.getPlaces().size(); i++) {
            PlanPlace place = plan.getPlaces().get(i);
            place.setSequenceNumber(i);
        }
    }

    private void addPlace(Plan plan, PlanUpdateMessage updateMessage) {
        String newPlaceId = updateMessage.getPlaceId();
        int newSequenceNumber = plan.getPlaces().size();

        // Create and add the new place
        PlanPlace newPlanPlace = new PlanPlace();
        newPlanPlace.setPlan(plan);
        newPlanPlace.setPlaceId(newPlaceId);
        newPlanPlace.setSequenceNumber(newSequenceNumber);
        newPlanPlace.setGooglePlaceId(updateMessage.getGooglePlaceId());
        newPlanPlace.setStaySeconds(updateMessage.getStaySeconds());

        // Add the new place to the plan
        plan.getPlaces().add(newPlanPlace);
    }

    private void removePlace(Plan plan, PlanUpdateMessage updateMessage) {
        String placeId = updateMessage.getPlaceId();

        PlanPlace placeToRemove = plan.getPlaces().stream()
                .filter(place -> place.getPlaceId().equals(placeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid placeId for removal: " + placeId));

        plan.getPlaces().remove(placeToRemove);

        // Update sequence numbers
        for (int i = 0; i < plan.getPlaces().size(); i++) {
            PlanPlace place = plan.getPlaces().get(i);
            place.setSequenceNumber(i);
        }
    }
}
