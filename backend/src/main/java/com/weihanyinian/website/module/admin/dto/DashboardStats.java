package com.weihanyinian.website.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long articleCount;
    private long guestbookCount;
    private long commentCount;
    private long todayVisits;
    private long weekVisits;
    private long totalVisits;
    private List<Map<String, Object>> topPaths;
    private List<Map<String, Object>> topIps;
    private List<Map<String, Object>> dailyVisits;
}
