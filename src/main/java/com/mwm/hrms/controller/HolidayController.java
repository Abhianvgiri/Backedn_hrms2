package com.mwm.hrms.controller;

import com.mwm.hrms.entity.Holiday;
import com.mwm.hrms.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@CrossOrigin(origins = "*")
public class HolidayController {

    @Autowired
    private HolidayRepository holidayRepository;

    @GetMapping
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(holidayRepository.findAllByOrderByHolidayDateAsc());
    }

    @PostMapping
    public ResponseEntity<Holiday> addHoliday(@RequestBody Holiday holiday) {
        return ResponseEntity.ok(holidayRepository.save(holiday));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHoliday(@PathVariable Long id, @RequestBody Holiday holidayDetails) {
        return holidayRepository.findById(id).map(holiday -> {
            holiday.setName(holidayDetails.getName());

            holiday.setHolidayDate(holidayDetails.getHolidayDate());

            holiday.setType(holidayDetails.getType());
            holiday.setDescription(holidayDetails.getDescription());
            return ResponseEntity.ok(holidayRepository.save(holiday));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
        holidayRepository.deleteById(id);
        return ResponseEntity.ok().body("Holiday deleted successfully");
    }
}