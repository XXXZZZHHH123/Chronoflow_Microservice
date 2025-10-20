package nus.edu.u.repositories.email;

import nus.edu.u.domain.dataObject.email.EmailMessageDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessageDO, String> {}
