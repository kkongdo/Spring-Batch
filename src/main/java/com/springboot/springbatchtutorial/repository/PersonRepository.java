package com.springboot.springbatchtutorial.repository;

import com.springboot.springbatchtutorial.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
