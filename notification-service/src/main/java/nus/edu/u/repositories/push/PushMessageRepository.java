package nus.edu.u.repositories.push;

import nus.edu.u.domain.dataObject.push.PushMessageDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PushMessageRepository extends JpaRepository<PushMessageDO, String> {}
