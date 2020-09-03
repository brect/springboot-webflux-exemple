package com.blimas.webfluxexemple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Optional;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private TodoRepository todoRepository;

    private TransactionTemplate transactionTemplate;

    @Qualifier("jdbcScheduler")
    private Scheduler jdbcSchedule;

    public TodoController(TodoRepository todoRepository, TransactionTemplate transactionTemplate, Scheduler jdbcSchedule) {
        this.todoRepository = todoRepository;
        this.transactionTemplate = transactionTemplate;
        this.jdbcSchedule = jdbcSchedule;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Mono<Optional<Todo>> findById(@PathVariable("id") Long id) {
        return Mono.just(this.todoRepository.findById(id));
    }


    @GetMapping()
    public Flux<Todo> findAll() {
        return Flux.defer(
                () -> Flux.fromIterable(
                        this.todoRepository.findAll()
                ).subscribeOn(jdbcSchedule)
        );
    }


    @PostMapping
    public Mono<Todo> save(@RequestBody Todo todo) {
        Mono op = Mono.fromCallable(() -> this.transactionTemplate.execute(action -> {
            Todo newTodo = this.todoRepository.save(todo);
            return newTodo;
        }));

        return op;
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> remove(@PathVariable("id") Long id) {
        return Mono.fromCallable(() -> this.transactionTemplate.execute(
                action -> {
                    this.todoRepository.deleteById(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                }
        )).subscribeOn(jdbcSchedule);
    }

}
