package com.in.cafe.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.in.cafe.POJO.Bill;

public interface BillDao extends JpaRepository<Bill, Integer> {

}
