package com.bkb.scanner.controller;

import com.bkb.scanner.dto.DashboardActivityDto;
import com.bkb.scanner.dto.DashboardStatsDto;
import com.bkb.scanner.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard statistics
     * @param timeFilter - 'week', 'month', or 'quarter'
     * @return Dashboard statistics including overview, trends, risk distribution
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard:view') or hasAuthority('case:read')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(
            @RequestParam(defaultValue = "week") String timeFilter) {

        System.out.println("Fetching dashboard stats with timeFilter: " + timeFilter);
        DashboardStatsDto stats = dashboardService.getDashboardStats(timeFilter);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get dashboard activity (recent and urgent cases)
     * @param limit - number of recent cases to return (max 50)
     * @param includeUrgent - whether to include urgent cases
     * @return Recent cases and optionally urgent cases
     */
    @GetMapping("/activity")
    @PreAuthorize("hasAuthority('dashboard:view') or hasAuthority('case:read')")
    public ResponseEntity<DashboardActivityDto> getDashboardActivity(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "true") boolean includeUrgent) {

        System.out.println("Fetching dashboard activity with limit: " + limit + ", includeUrgent: " + includeUrgent);

        // Validate limit
        if (limit < 1) limit = 1;
        if (limit > 50) limit = 50;

        DashboardActivityDto activity = dashboardService.getDashboardActivity(limit, includeUrgent);
        return ResponseEntity.ok(activity);
    }
}