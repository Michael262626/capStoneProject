package com.africa.semiclon.capStoneProject.services.interfaces;

import com.africa.semiclon.capStoneProject.data.models.User;
import com.africa.semiclon.capStoneProject.dtos.request.CreateUserRequest;
import com.africa.semiclon.capStoneProject.dtos.response.CreateUserResponse;

public interface UserService {
    
    User getById(long l);

    CreateUserResponse register(CreateUserRequest createUserRequest);
}
