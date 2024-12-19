package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
} 