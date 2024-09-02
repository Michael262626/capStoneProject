package com.africa.semiclon.capStoneProject.services.implemenation;

import com.africa.semiclon.capStoneProject.data.models.*;
import com.africa.semiclon.capStoneProject.data.repository.AddressRepository;
import com.africa.semiclon.capStoneProject.data.repository.AgentRepository;
import com.africa.semiclon.capStoneProject.data.repository.WasteCollectionRepository;
import com.africa.semiclon.capStoneProject.dtos.request.*;
import com.africa.semiclon.capStoneProject.dtos.response.RegisterAgentResponse;
import com.africa.semiclon.capStoneProject.dtos.response.SendWasteDetailResponse;
import com.africa.semiclon.capStoneProject.dtos.response.UpdateAgentProfileResponse;
import com.africa.semiclon.capStoneProject.exception.AgentExistAlreadyException;
import com.africa.semiclon.capStoneProject.exception.AgentNotFoundException;
import com.africa.semiclon.capStoneProject.exception.InvalidEmailFormatException;
import com.africa.semiclon.capStoneProject.exception.InvalidPasswordFormatException;
import com.africa.semiclon.capStoneProject.exception.CollectWasteResponse;
import com.africa.semiclon.capStoneProject.response.ViewWasteCollectedResponse;
import com.africa.semiclon.capStoneProject.security.services.interfaces.AuthServices;
import com.africa.semiclon.capStoneProject.services.interfaces.AgentService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentServiceImplementation implements AgentService {


    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AgentRepository agentRepository;
    private final AddressRepository addressRepository;
    private final WasteCollectionRepository wasteCollectionRepository;



    public AgentServiceImplementation(ModelMapper modelMapper, PasswordEncoder passwordEncoder, AgentRepository agentRepository, AuthServices authServices, AddressRepository addressRepository, WasteCollectionRepository wasteCollectionRepository) {
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.agentRepository = agentRepository;
        this.addressRepository = addressRepository;

        this.wasteCollectionRepository = wasteCollectionRepository;
    }

    @Override
    public RegisterAgentResponse createAccount(RegisterAgentRequest request) {
        validateEmail(request.getEmail());
        verifyAgentExistence(request.getEmail());
        Agent agent = modelMapper.map(request, Agent.class);
        agent.setAuthorities(new HashSet<>());
        var authorities = agent.getAuthorities();
        authorities.add(Authority.AGENT);
        agent.setPassword(passwordEncoder.encode(request.getPassword()));
        validatePassword(request.getPassword());
        agentRepository.save(agent);
        RegisterAgentResponse response = new RegisterAgentResponse();
        response.setEmail(request.getEmail());
        response.setMessage("registered successfully");
        response.setDateCreated(LocalDateTime.now());
        response.setUsername(request.getUsername());
        response.setAgentId(request.getId());
        return response;

    }

    @Override
    public List<Agent> getAgents() {
        return agentRepository.findAll();
    }


    @Override
    public SendWasteDetailResponse sendWasteDetails(SendWasteDetailRequest request) {
        return null;

    }

    @Override
    public Agent findAgentById(FindAgentRequest findAgentRequest) {
        return agentRepository.findById(findAgentRequest.getId()).orElseThrow(() -> new AgentNotFoundException("Agent not found"));

    }

    @Override
    public UpdateAgentProfileResponse updateProfile(UpdateAgentProfileRequest request) {
        Agent agent = agentRepository.findByEmail(request.getEmail());
        if (agent != null) {
            Address address = modelMapper.map(request.getAddress(), Address.class);
            addressRepository.save(address);
            agent.setAddressId(address);
            agentRepository.save(agent);
            return new UpdateAgentProfileResponse("Profile updated successfully");
        } else {
            throw new AgentNotFoundException("Agent Not Found Exception");
        }
    }

    @Override
    public CollectWasteResponse collectWaste(CollectWasteRequest collectWasteRequest) {
        WasteCollection wasteCollection = modelMapper.map(collectWasteRequest, WasteCollection.class);
        Agent agent = wasteCollection.getAgentId();
        agentRepository.save(agent);
        CollectWasteResponse response = new CollectWasteResponse();
        response.setMessage("Waste collected successfully");
        response.setWasteWeigh(collectWasteRequest.getWasteWeigh());
        response.setWasteCategory(collectWasteRequest.getWasteCategory());
        response.setAgentId(collectWasteRequest.getAgentId());
        response.setUserName(collectWasteRequest.getUsername());
//        response.setUserId(collectWasteRequest.getUserId());
        return response;
    }

//    public void startWeighingProcess(String portName, CollectWasteRequest collectWasteRequest) {
//        ScaleReader reader = new ScaleReader();
//        reader.readFromScale(portName, weight -> {
//            collectWasteRequest.setWasteWeigh(Double.valueOf(weight));
//            CollectWasteResponse response = collectWaste(collectWasteRequest);
//            response.setMessage("Waste weigh collected successfully");
//        });
//    }

    @Override


        public List<ViewWasteCollectedResponse> viewAllWasteCollected(ViewWasteCollectedRequest request) {
            List<WasteCollection> collections = wasteCollectionRepository.findByAgentId_Id(request.getAgentId());
            return collections.stream()
                    .map(collection -> new ViewWasteCollectedResponse(
                            collection.getWasteCategory(),
                            collection.getDateAndTimeCollected(),
                            collection.getAgentId(),
                            collection.getWasteWeigh()))
                            .collect(Collectors.toList());
        }


    private void verifyAgentExistence(String email) {
        Agent agent = agentRepository.findByEmail(email);
        if (agent != null) {
            throw new AgentExistAlreadyException(String.format("%s already exist", email));
        }
    }

    private void validateEmail(String email) {
        String regex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)" +
                "*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
        if (!email.matches(regex)) {
            throw new InvalidEmailFormatException("invalid email format");
        }

    }

    private void validatePassword(String password) {
        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";
        if (!password.matches(regex)) {
            throw new InvalidPasswordFormatException("password must be 8 character, " +
                    "must include special characters, number,upper case, lower case ");
        }

    }

    public void initiateWasteCollection(String portName) {
        CollectWasteRequest request = new CollectWasteRequest();
        startWeighingProcess(portName, request);
    }

    @Override
    public void startWeighingProcess(String portName, CollectWasteRequest request) {

    }


}


