package com.emailSender.vomychatTask.repository;

import com.emailSender.vomychatTask.model.EmailRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<EmailRequest, Long> {

}
