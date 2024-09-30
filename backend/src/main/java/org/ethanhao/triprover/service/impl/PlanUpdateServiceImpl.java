package org.ethanhao.triprover.service.impl;

import jakarta.transaction.Transactional;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanUpdateServiceImpl implements PlanUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PlanUpdateServiceImpl.class);
    private final PlanRepository planRepository;

    @Autowired
    public PlanUpdateServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public void updatePlanWithMessage(Long planId, PlanUpdateMessage updateMessage) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

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

        // Save the updated plan
        planRepository.save(plan);
    }

    // Private helper methods

    private void reorderPlaces(Plan plan, PlanUpdateMessage updateMessage) {
        int fromIndex = updateMessage.getFromIndex();
        int toIndex = updateMessage.getToIndex();

        List<PlanPlace> places = plan.getPlaces();

        if (fromIndex < 0 || fromIndex >= places.size() || toIndex < 0 || toIndex >= places.size()) {
            throw new IllegalArgumentException("Invalid indices for reordering");
        }

        // Move the place
        PlanPlace movedPlace = places.remove(fromIndex);
        places.add(toIndex, movedPlace);

        // Update sequence numbers
        for (int i = 0; i < places.size(); i++) {
            PlanPlace place = places.get(i);
            // log in server console
            System.out.println("Place: " + place.getPlaceId());
            place.setSequenceNumber(i);
        }
    }

    private void addPlace(Plan plan, PlanUpdateMessage updateMessage) {
        String newPlaceId = updateMessage.getPlaceId();
        int newSequenceNumber = plan.getPlaces().size();

        // Create a new PlanPlace instance
        PlanPlace newPlanPlace = new PlanPlace();
        newPlanPlace.setPlan(plan);
        newPlanPlace.setPlaceId(newPlaceId);
        newPlanPlace.setSequenceNumber(newSequenceNumber);

        // Add the new place to the plan
        plan.getPlaces().add(newPlanPlace);
    }

    private void removePlace(Plan plan, PlanUpdateMessage updateMessage) {
        int index = updateMessage.getIndex();

        List<PlanPlace> places = plan.getPlaces();

        if (index < 0 || index >= places.size()) {
            throw new IllegalArgumentException("Invalid index for removal");
        }

        places.remove(index);

        // Update sequence numbers
        for (int i = 0; i < places.size(); i++) {
            PlanPlace place = places.get(i);
            place.setSequenceNumber(i);
        }
    }
}
