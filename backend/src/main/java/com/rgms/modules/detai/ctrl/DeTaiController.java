package com.rgms.modules.detai.ctrl;

import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.KetQuaPBRequest;
import com.rgms.modules.detai.dto.LapToPhanBienRequest;
import com.rgms.modules.detai.dto.PageDeTaiResponse;
import com.rgms.modules.detai.dto.PhanBienDeXuatRequest;
import com.rgms.modules.detai.dto.PhanBienDeXuatResponse;
import com.rgms.modules.detai.dto.PhanHoiHopDongRequest;
import com.rgms.modules.detai.dto.SoanHopDongRequest;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.dto.TuChoiHoSoRequest;
import com.rgms.modules.detai.dto.XetDuyetPBRequest;
import com.rgms.modules.detai.dto.YeuCauBoSungRequest;
import com.rgms.modules.detai.service.ResearchTopicService;
import com.rgms.shared.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/de-tai")
@Tag(name = "DeTai", description = "API quản lý đề tài nghiên cứu khoa học và các bước xử lý Luồng 1")
@SecurityRequirement(name = "bearerAuth")
public class DeTaiController {

    private final ResearchTopicService topicService;

    @PostMapping
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV tạo đề tài mới",
            description = "Tạo đề tài nghiên cứu ở trạng thái DRAFT. API kiểm tra kỳ NCKH phải đang mở, gán chủ nhiệm là giảng viên đăng nhập và tự sinh mã số dạng NCKH-[Năm]-000X."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đề tài nháp thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc kỳ NCKH đã đóng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò GIANG_VIEN"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<DeTaiResponse> taoDeTai(
            @Valid @RequestBody TaoDeTaiRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        DeTaiResponse response = topicService.taoDeTai(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Danh sách đề tài",
            description = "Trả về danh sách đề tài có phân trang và lọc theo trạng thái. Giảng viên chỉ thấy đề tài do chính mình làm chủ nhiệm; NCKH và các vai trò khác đã đăng nhập thấy toàn bộ đề tài."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = PageDeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Tham số phân trang không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<PageDeTaiResponse> danhSach(
            @Parameter(description = "Trạng thái đề tài cần lọc, ví dụ DRAFT hoặc CHO_PNCKH_XEM_XET")
            @RequestParam(required = false) String trangThai,
            @Parameter(description = "Số trang, bắt đầu từ 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @Parameter(description = "Kích thước trang, tối đa 50")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size phải >= 1") @Max(value = 50, message = "size tối đa là 50") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.danhSach(trangThai, PageRequest.of(page, size), currentUser));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Chi tiết đề tài",
            description = "Trả về thông tin chi tiết của một đề tài, bao gồm danh sách tài liệu đính kèm và lịch sử audit. Nếu người gọi là giảng viên, hệ thống chỉ cho phép xem đề tài do chính giảng viên đó làm chủ nhiệm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Giảng viên không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<DeTaiDetailResponse> layChiTiet(
            @Parameter(description = "ID đề tài cần xem", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.layChiTiet(id, currentUser));
    }

    @PostMapping("/{id}/phan-bien-de-xuat")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV thêm phản biện đề xuất",
            description = "Thêm chuyên gia phản biện đề xuất cho đề tài nháp. API chỉ cho phép chủ nhiệm đề tài thao tác khi đề tài còn ở trạng thái DRAFT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm phản biện đề xuất thành công",
                    content = @Content(schema = @Schema(implementation = PhanBienDeXuatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu phản biện đề xuất không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ nhiệm đề tài hoặc không có vai trò GIANG_VIEN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Đề tài không còn ở trạng thái DRAFT")
    })
    public ResponseEntity<PhanBienDeXuatResponse> themPhanBienDeXuat(
            @Parameter(description = "ID đề tài cần thêm phản biện đề xuất", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PhanBienDeXuatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(topicService.themPhanBienDeXuat(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/gui-ho-so")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV gửi hồ sơ đến Phòng NCKH",
            description = "Chuyển đề tài từ DRAFT sang CHO_PNCKH_XEM_XET. API kiểm tra chủ nhiệm đề tài, phải có file thuyết minh và ít nhất một phản biện đề xuất trước khi gửi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gửi hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu file thuyết minh hoặc thiếu phản biện đề xuất"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> guiHoSo(
            @Parameter(description = "ID đề tài cần gửi hồ sơ", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.guiHoSo(id, currentUser.getId()));
    }

    @PostMapping("/{id}/tiep-nhan")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH tiếp nhận hồ sơ",
            description = "Chuyển đề tài từ CHO_PNCKH_XEM_XET sang DANG_XEM_XET_BOI_PNCKH để bắt đầu sơ thẩm hồ sơ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tiếp nhận hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> tiepNhan(
            @Parameter(description = "ID đề tài cần tiếp nhận", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.tiepNhan(id, currentUser.getId()));
    }

    @PostMapping("/{id}/yeu-cau-bo-sung")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH yêu cầu bổ sung hồ sơ",
            description = "Tạo feedback loại BO_SUNG_HO_SO với nội dung và deadline phản hồi, sau đó chuyển đề tài từ DANG_XEM_XET_BOI_PNCKH sang CHO_BO_SUNG_HO_SO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yêu cầu bổ sung thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu nội dung hoặc deadline phản hồi không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> yeuCauBoSung(
            @Parameter(description = "ID đề tài cần yêu cầu bổ sung", required = true) @PathVariable Long id,
            @Valid @RequestBody YeuCauBoSungRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.yeuCauBoSung(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/nop-bo-sung")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV nộp lại hồ sơ bổ sung",
            description = "Chủ nhiệm đề tài xác nhận đã nộp bổ sung theo feedback BO_SUNG_HO_SO còn hiệu lực. Feedback được đánh dấu DA_XU_LY và đề tài quay lại DANG_XEM_XET_BOI_PNCKH."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nộp bổ sung thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Không có feedback đang chờ xử lý hoặc đã quá deadline"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> nopBoSung(
            @Parameter(description = "ID đề tài cần nộp bổ sung", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.gvNopBoSung(id, currentUser.getId()));
    }

    @PostMapping("/{id}/tu-choi-ho-so")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH từ chối hồ sơ sơ thẩm",
            description = "Từ chối hồ sơ trong giai đoạn DANG_XEM_XET_BOI_PNCKH, ghi audit mức WARNING kèm lý do và chuyển đề tài sang BI_TU_CHOI."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Từ chối hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu lý do từ chối"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> tuChoiHoSo(
            @Parameter(description = "ID đề tài cần từ chối", required = true) @PathVariable Long id,
            @Valid @RequestBody TuChoiHoSoRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.tuChoiHoSo(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/lap-to-phan-bien")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH lập tổ phản biện",
            description = "Tạo tổ phản biện cho đề tài đang DANG_XEM_XET_BOI_PNCKH. Request phải có ít nhất một thành viên vai trò TO_PHAN_BIEN và deadline nộp kết quả."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lập tổ phản biện thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Danh sách thành viên rỗng, thành viên không tồn tại hoặc sai vai trò"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Đề tài đã có tổ phản biện hoặc trạng thái không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> lapToPhanBien(
            @Parameter(description = "ID đề tài cần lập tổ phản biện", required = true) @PathVariable Long id,
            @Valid @RequestBody LapToPhanBienRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.lapToPhanBien(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/nop-ket-qua-pb")
    @PreAuthorize("hasRole('TO_PHAN_BIEN')")
    @Operation(
            summary = "Thành viên tổ phản biện nộp kết quả",
            description = "Thành viên phản biện nộp kết quả CHAP_NHAN, CAN_SUA hoặc TU_CHOI. API kiểm tra người gọi thuộc tổ phản biện, chưa nộp trước đó và còn trong deadline."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nộp kết quả phản biện thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Kết quả không hợp lệ hoặc quá deadline"),
            @ApiResponse(responseCode = "403", description = "Không thuộc tổ phản biện của đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Đề tài không ở trạng thái phản biện hoặc thành viên đã nộp")
    })
    public ResponseEntity<DeTaiResponse> nopKetQuaPb(
            @Parameter(description = "ID đề tài cần nộp kết quả phản biện", required = true) @PathVariable Long id,
            @Valid @RequestBody KetQuaPBRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.pbNopKetQua(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/xet-duyet-pb")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH xét duyệt kết quả phản biện",
            description = "Xét duyệt kết quả phản biện với quyết định CHAP_NHAN, YEU_CAU_SUA hoặc TU_CHOI. Quyết định yêu cầu sửa sẽ tạo feedback KET_QUA_PB và deadline nộp lại."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xét duyệt phản biện thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Quyết định không hợp lệ hoặc thiếu nội dung/deadline khi yêu cầu sửa"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> xetDuyetPb(
            @Parameter(description = "ID đề tài cần xét duyệt phản biện", required = true) @PathVariable Long id,
            @Valid @RequestBody XetDuyetPBRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.xetDuyetPB(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/nop-lai-thuyet-minh")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV nộp lại thuyết minh sau phản biện",
            description = "Chủ nhiệm đề tài xác nhận đã upload thuyết minh chỉnh sửa sau feedback KET_QUA_PB, còn trong deadline. Feedback được đánh dấu DA_XU_LY và đề tài quay lại DANG_PHAN_BIEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nộp lại thuyết minh thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Chưa upload thuyết minh mới hoặc đã quá deadline"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> nopLaiThuyetMinh(
            @Parameter(description = "ID đề tài cần nộp lại thuyết minh", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.gvNopLaiThuyetMinh(id, currentUser.getId()));
    }

    @PostMapping("/{id}/soan-hop-dong")
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH soạn hợp đồng đề tài",
            description = "Tạo dự thảo hợp đồng cho đề tài ở trạng thái DANG_LAP_HOP_DONG. Hợp đồng được tạo ở trạng thái CHO_PHAN_HOI và chờ giảng viên phản hồi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Soạn hợp đồng thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thông tin hợp đồng không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Đề tài đã có hợp đồng hoặc trạng thái không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> soanHopDong(
            @Parameter(description = "ID đề tài cần soạn hợp đồng", required = true) @PathVariable Long id,
            @Valid @RequestBody SoanHopDongRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.soanHopDong(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/phan-hoi-hop-dong")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV phản hồi dự thảo hợp đồng",
            description = "Chủ nhiệm đề tài đồng ý hoặc góp ý dự thảo hợp đồng. Nếu đồng ý, hợp đồng chuyển sang CHO_KY; nếu không đồng ý, tạo feedback HOP_DONG và hợp đồng chuyển YEU_CAU_SUA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phản hồi hợp đồng thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu nội dung góp ý khi không đồng ý"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài hoặc hợp đồng không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> phanHoiHopDong(
            @Parameter(description = "ID đề tài cần phản hồi hợp đồng", required = true) @PathVariable Long id,
            @Valid @RequestBody PhanHoiHopDongRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.phanHoiHopDong(id, request, currentUser.getId()));
    }

    @PostMapping(value = "/{id}/ky-hop-dong", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('NCKH')")
    @Operation(
            summary = "PNCKH ký hợp đồng",
            description = "Xác nhận hợp đồng đã ký bằng file scan PDF và ngày ký. API kiểm tra hợp đồng ở trạng thái CHO_KY, file PDF dưới 20MB, sau đó chuyển đề tài sang DANG_THUC_HIEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ký hợp đồng thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu ngày ký, file rỗng, file không phải PDF hoặc quá 20MB"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "409", description = "Trạng thái đề tài hoặc hợp đồng không hợp lệ")
    })
    public ResponseEntity<DeTaiResponse> kyHopDong(
            @Parameter(description = "ID đề tài cần ký hợp đồng", required = true) @PathVariable Long id,
            @Parameter(description = "File scan hợp đồng đã ký, định dạng PDF, tối đa 20MB", required = true)
            @RequestPart("fileScan") MultipartFile fileScan,
            @Parameter(description = "Ngày ký hợp đồng, định dạng yyyy-MM-dd", required = true)
            @RequestParam("ngayKy") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKy,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.kyHopDong(id, fileScan, ngayKy, currentUser.getId()));
    }
}
