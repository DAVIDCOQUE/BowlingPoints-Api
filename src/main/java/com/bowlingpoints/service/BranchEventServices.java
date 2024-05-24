package com.bowlingpoints.service;


import com.bowlingpoints.entity.Branch;
import com.bowlingpoints.entity.BranchEvent;
import com.bowlingpoints.repository.BranchEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BranchEventServices {

    @Autowired
    BranchEventRepository branchEventRepository;


    public BranchEvent findById(){
        return branchEventRepository.findById(3).get();
    }
}
