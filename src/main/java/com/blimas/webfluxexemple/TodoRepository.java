package com.blimas.webfluxexemple;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/*
* Para MySQL utilizar CrudRepository
* Para os demais bancos utilizar ReactiveCrudRepository
*/

@Repository
public interface TodoRepository extends CrudRepository<Todo, Long> {

}
