package com.example.demo.services;
import com.example.demo.models.Complaint;
import com.example.demo.models.ComplaintAction;
import com.example.demo.models.Staff;
import org.springframework.stereotype.Service;
import com.example.demo.enums.*;
import com.example.demo.dto.*;
import com.example.demo.repository.*;
import com.example.demo.repository.StaffRepository;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StaffService {
    private final ComplaintRepository complaintRepository;
    private final ComplaintActionRepository complaintActionRepository;
    private final StaffRepository staffRepository;
    public StaffService( ComplaintRepository complaintRepository,ComplaintActionRepository complaintActionRepository,StaffRepository staffRepository){
        this.complaintRepository=complaintRepository;
        this.complaintActionRepository=complaintActionRepository;
        this.staffRepository=staffRepository;
    }

  public List<ComplaintResponse> getAssignedComplaints(Long staffId)
   {
      Staff staff =  staffRepository.findById(staffId).orElseThrow(()-> new RuntimeException("Staff not found"));
      List<Complaint> complaints = complaintRepository.findByAssignedStaff_Id(staffId);
       List<ComplaintResponse> responseList = new ArrayList<>();

       for(Complaint savedComplaint:complaints)
       {
       ComplaintResponse complaintResponse = new ComplaintResponse();
       complaintResponse.setComplaintId(savedComplaint.getComplaintId());
       complaintResponse.setCategory(savedComplaint.getCategory());
       complaintResponse.setDescription(savedComplaint.getDescription());
       complaintResponse.setStatus(savedComplaint.getStatus());
       complaintResponse.setSubmissionDate(savedComplaint.getCreatedAt());
       complaintResponse.setExpectedResolutionDate(savedComplaint.getExpectedResolutionDate());
       complaintResponse.setId(savedComplaint.getId());
       responseList.add(complaintResponse);
       }
       
       return responseList;
    
    }


    public ComplaintResponse updateComplaintStatus(Long compalintId, ComplaintStatus status,Long staffId){
       Complaint complaint= complaintRepository.findById(compalintId)
        .orElseThrow(()->new RuntimeException("comaplint not found"));
        
     if(complaint.getAssignedStaff()==null||!complaint.getAssignedStaff().getId().equals(staffId)){
        throw new RuntimeException("access denied");
     }
     
     complaint.setStatus(status);
     complaint.setUpdatedAt(LocalDateTime.now());
     Complaint savedComplaint = complaintRepository.save(complaint);
     ComplaintResponse complaintResponse = new ComplaintResponse();
       complaintResponse.setComplaintId(savedComplaint.getComplaintId());
       complaintResponse.setCategory(savedComplaint.getCategory());
       complaintResponse.setDescription(savedComplaint.getDescription());
       complaintResponse.setStatus(savedComplaint.getStatus());
       complaintResponse.setSubmissionDate(savedComplaint.getCreatedAt());
       complaintResponse.setExpectedResolutionDate(savedComplaint.getExpectedResolutionDate());
       complaintResponse.setId(savedComplaint.getId());
        return complaintResponse;
    }


    public void addComplaintAction(ComplaintActionRequest request) {


    Complaint complaint = complaintRepository.findById(request.getComplaintId())
            .orElseThrow(() -> new RuntimeException("Complaint not found"));

    Staff staff = staffRepository.findById(request.getStaffId())
            .orElseThrow(() -> new RuntimeException("Staff not found"));

    if (complaint.getAssignedStaff() == null ||
        !complaint.getAssignedStaff().getId().equals(staff.getId())) {
        throw new RuntimeException("Access denied");
    }

    ComplaintAction action = new ComplaintAction();
    action.setComplaint(complaint);
    action.setStaff(staff);
    action.setActionNote(request.getActionNote());
    action.setStatus(request.getStatus());
    action.setActionAt(LocalDateTime.now());

    complaintActionRepository.save(action);
}

}