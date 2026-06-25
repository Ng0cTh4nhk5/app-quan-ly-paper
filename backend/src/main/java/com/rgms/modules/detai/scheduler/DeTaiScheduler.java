package com.rgms.modules.detai.scheduler;

import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.service.ResearchTopicService;
import com.rgms.shared.enums.TopicState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DeTaiScheduler — Scheduler auto-treo đề tài quá hạn (E1, E2, E6, E8).
 *
 * Cấu hình:
 *   - Chạy mỗi ngày 01:00 AM (cron: "0 0 1 * * ?")
 *   - System Actor ID = 1L (Admin account, seed trong V1_1__seed_data.sql)
 *   - draft_expiry_days: lấy từ cau_hinh_he_thong (mặc định 30 ngày)
 *
 * Các trường hợp auto-treo:
 *   [E1] DRAFT quá {draft_expiry_days} ngày từ ngayTao.
 *   [E2] CHO_BO_SUNG_HO_SO quá deadline feedback (deadline_phan_hoi trong Feedback entity).
 *   [E6] CHO_CHINH_SUA_THUYET_MINH quá hạn (deadline trong ToPhanBien).
 *   [E8] DANG_LAP_HOP_DONG quá hạn GV xác nhận HĐ.
 *
 * Tham chiếu: sop-member-a.md Giai đoạn 2 — HE_THONG_TREO event.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeTaiScheduler {

    private final DeTaiRepository deTaiRepository;
    private final ResearchTopicService researchTopicService;

    /** Long ID của tài khoản Admin system (xem V1_1__seed_data.sql — id=1) */
    private static final Long SYSTEM_ACTOR_ID = 1L;

    /** Số ngày tối đa ở DRAFT trước khi treo (E1). Overridable từ config. */
    @Value("${rgms.scheduler.draft-expiry-days:30}")
    private int draftExpiryDays;

    /** Số ngày tối đa ở CHO_CHINH_SUA_THUYET_MINH trước khi treo (E6). */
    @Value("${rgms.scheduler.sua-tm-expiry-days:14}")
    private int suaTmExpiryDays;

    /** Số ngày tối đa ở DANG_LAP_HOP_DONG trước khi treo (E8). */
    @Value("${rgms.scheduler.hop-dong-expiry-days:7}")
    private int hopDongExpiryDays;

    // ──────────────────────────────────────────────────────────────────────────
    // E1: Auto-treo DRAFT quá hạn (mỗi ngày 01:00 AM)
    // ──────────────────────────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 1 * * ?")
    public void treoDeTriTaiDraftQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(draftExpiryDays);

        List<DeTai> danhSach = deTaiRepository.findAllByStatus(TopicState.DRAFT);
        int count = 0;

        for (DeTai deTai : danhSach) {
            if (deTai.getCreatedAt() != null && deTai.getCreatedAt().isBefore(cutoff)) {
                try {
                    researchTopicService.autoTreoDeTai(deTai.getId(), SYSTEM_ACTOR_ID);
                    count++;
                    log.info("[Scheduler-E1] Auto-treo DRAFT id={} createdAt={}", deTai.getId(), deTai.getCreatedAt());
                } catch (Exception e) {
                    log.error("[Scheduler-E1] Lỗi khi treo deTai id={}: {}", deTai.getId(), e.getMessage());
                }
            }
        }

        if (count > 0) {
            log.info("[Scheduler-E1] Đã treo {} đề tài DRAFT quá {} ngày.", count, draftExpiryDays);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // E2: Auto-treo CHO_BO_SUNG_HO_SO quá hạn bổ sung (ngoai-le.md mục 1)
    // ──────────────────────────────────────────────────────────────────────────

    /** Số ngày tối đa ở CHO_BO_SUNG_HO_SO trước khi treo (E2). */
    @Value("${rgms.scheduler.bo-sung-expiry-days:30}")
    private int boSungExpiryDays;

    @Scheduled(cron = "0 2 1 * * ?")
    public void treoDeTriTaiBoSungQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(boSungExpiryDays);

        List<DeTai> danhSach = deTaiRepository.findAllByStatus(TopicState.CHO_BO_SUNG_HO_SO);
        int count = 0;

        for (DeTai deTai : danhSach) {
            if (deTai.getUpdatedAt() != null && deTai.getUpdatedAt().isBefore(cutoff)) {
                try {
                    researchTopicService.autoTreoDeTai(deTai.getId(), SYSTEM_ACTOR_ID);
                    count++;
                    log.info("[Scheduler-E2] Auto-treo CHO_BO_SUNG_HO_SO id={} updatedAt={}",
                            deTai.getId(), deTai.getUpdatedAt());
                } catch (Exception e) {
                    log.error("[Scheduler-E2] Lỗi khi treo deTai id={}: {}", deTai.getId(), e.getMessage());
                }
            }
        }

        if (count > 0) {
            log.info("[Scheduler-E2] Đã treo {} đề tài CHO_BO_SUNG_HO_SO quá {} ngày.", count, boSungExpiryDays);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // E6: Auto-treo CHO_CHINH_SUA_THUYET_MINH quá hạn
    // ──────────────────────────────────────────────────────────────────────────

    @Scheduled(cron = "0 5 1 * * ?")
    public void treoDeTriTaiSuaTmQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(suaTmExpiryDays);

        List<DeTai> danhSach = deTaiRepository.findAllByStatus(TopicState.CHO_CHINH_SUA_THUYET_MINH);
        int count = 0;

        for (DeTai deTai : danhSach) {
            if (deTai.getUpdatedAt() != null && deTai.getUpdatedAt().isBefore(cutoff)) {
                try {
                    researchTopicService.autoTreoDeTai(deTai.getId(), SYSTEM_ACTOR_ID);
                    count++;
                    log.info("[Scheduler-E6] Auto-treo CHO_CHINH_SUA_TM id={}", deTai.getId());
                } catch (Exception e) {
                    log.error("[Scheduler-E6] Lỗi deTai id={}: {}", deTai.getId(), e.getMessage());
                }
            }
        }

        if (count > 0) {
            log.info("[Scheduler-E6] Đã treo {} đề tài CHO_CHINH_SUA_TM quá {} ngày.", count, suaTmExpiryDays);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // E8: Auto-treo DANG_LAP_HOP_DONG quá hạn GV xác nhận
    // ──────────────────────────────────────────────────────────────────────────

    @Scheduled(cron = "0 10 1 * * ?")
    public void treoDeTriTaiHopDongQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(hopDongExpiryDays);

        List<DeTai> danhSach = deTaiRepository.findAllByStatus(TopicState.DANG_LAP_HOP_DONG);
        int count = 0;

        for (DeTai deTai : danhSach) {
            boolean gvChuaDongY = Boolean.FALSE.equals(deTai.getGvDaDongYHopDong());
            boolean quaHan = deTai.getUpdatedAt() != null
                    && deTai.getUpdatedAt().isBefore(cutoff);

            if (gvChuaDongY && quaHan) {
                try {
                    researchTopicService.autoTreoDeTai(deTai.getId(), SYSTEM_ACTOR_ID);
                    count++;
                    log.info("[Scheduler-E8] Auto-treo DANG_LAP_HOP_DONG id={}", deTai.getId());
                } catch (Exception e) {
                    log.error("[Scheduler-E8] Lỗi deTai id={}: {}", deTai.getId(), e.getMessage());
                }
            }
        }

        if (count > 0) {
            log.info("[Scheduler-E8] Đã treo {} đề tài DANG_LAP_HOP_DONG quá {} ngày.", count, hopDongExpiryDays);
        }
    }
}
