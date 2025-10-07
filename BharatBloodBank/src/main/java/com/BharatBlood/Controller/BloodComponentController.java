package com.BharatBlood.Controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BharatBlood.dto.BloodComponentDto;
import com.BharatBlood.service.BloodComponentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BloodComponentController {

	private final BloodComponentService bloodComponentService;

	@PostMapping("/components")
	public ResponseEntity<BloodComponentDto> createBloodComponent(@RequestBody BloodComponentDto bloodComponentDto) {
		BloodComponentDto savedComponent = bloodComponentService.saveBloodComponent(bloodComponentDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedComponent);
	}
	
    @GetMapping("/components")
    public ResponseEntity<List<BloodComponentDto>> getAllComponents() {
        List<BloodComponentDto> components = bloodComponentService.getAllComponents();
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/components/bank/{bankId}")
    public ResponseEntity<List<BloodComponentDto>> getComponentsByBank(@PathVariable Long bankId) {
        List<BloodComponentDto> components = bloodComponentService.getComponentsByBank(bankId);
        return ResponseEntity.ok(components);
    }
}
