package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.dto.GetPlan;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private PlanRepository planRepository;

    @Transactional(readOnly = true)
    @Override
    public List<GetPlan> getUserPlans(Long userId) {
        
        List<GetPlan> planDTOs = planRepository.findPlansWithRolesByUserId(userId);
        logger.info("Fetched {} plans for user ID: {}", planDTOs.size(), userId);

        return planDTOs;
    }
} 