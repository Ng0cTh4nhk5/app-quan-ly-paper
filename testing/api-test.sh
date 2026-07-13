#!/bin/bash
# ============================================================================
# Script Kiểm thử API RGMS
# Mục đích: Kiểm thử tự động tất cả các endpoint khắc phục lỗi với thông báo màu
# Cách dùng: bash api-test.sh
# ============================================================================

# Cấu hình
BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

# Mã màu hiển thị
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # Không màu (No Color)

# Biến đếm
PASS_COUNT=0
FAIL_COUNT=0
TOTAL_COUNT=0

# ============================================================================
# Hàm Trợ giúp (Helper Functions)
# ============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_test() {
    echo -e "\n${YELLOW}KIỂM THỬ: $1${NC}"
}

pass() {
    echo -e "${GREEN}✓ ĐẠT${NC}: $1"
    ((PASS_COUNT++))
    ((TOTAL_COUNT++))
}

fail() {
    echo -e "${RED}✗ LỖI${NC}: $1"
    ((FAIL_COUNT++))
    ((TOTAL_COUNT++))
}

# ============================================================================
# 1. Xác thực (Authentication)
# ============================================================================

print_header "1. XÁC THỰC"

# Đăng nhập dưới tài khoản GV01
print_test "Đăng nhập với tài khoản gv01"
GV_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "$CONTENT_TYPE" \
    -d '{"username": "gv01", "password": "test123"}')

GV_TOKEN=$(echo "$GV_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$GV_TOKEN" ]; then
    pass "Đăng nhập GV01 thành công"
else
    fail "Đăng nhập GV01 thất bại"
    echo "Phản hồi: $GV_RESPONSE"
fi

# Đăng nhập dưới tài khoản NCKH01
print_test "Đăng nhập với tài khoản nckh01"
NCKH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "$CONTENT_TYPE" \
    -d '{"username": "nckh01", "password": "test123"}')

NCKH_TOKEN=$(echo "$NCKH_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$NCKH_TOKEN" ]; then
    pass "Đăng nhập NCKH01 thành công"
else
    fail "Đăng nhập NCKH01 thất bại"
    echo "Phản hồi: $NCKH_RESPONSE"
fi

# Đăng nhập dưới tài khoản PB01
print_test "Đăng nhập với tài khoản pb01"
PB_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "$CONTENT_TYPE" \
    -d '{"username": "pb01", "password": "test123"}')

PB_TOKEN=$(echo "$PB_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$PB_TOKEN" ]; then
    pass "Đăng nhập PB01 thành công"
else
    fail "Đăng nhập PB01 thất bại"
    echo "Phản hồi: $PB_RESPONSE"
fi

# ============================================================================
# RT-01: API can-actions (Hành động khả dụng)
# ============================================================================

print_header "2. RT-01: API CAN-ACTIONS"

# Kiểm thử với vai trò GV01 (chủ đề tài) - Đề tài 1 (CHO_PNCKH_XEM_XET)
print_test "RT-01.1: Lấy can-actions của đề tài 1 dưới quyền GV01 (chủ đề tài)"
CAN_ACTIONS_GV=$(curl -s -X GET "$BASE_URL/de-tai/1/can-actions" \
    -H "Authorization: Bearer $GV_TOKEN")

if echo "$CAN_ACTIONS_GV" | grep -q '"canWithdraw":true'; then
    pass "Giảng viên có quyền rút đề tài ở trạng thái CHO_PNCKH_XEM_XET"
else
    fail "Giảng viên không có quyền rút đề tài ở trạng thái CHO_PNCKH_XEM_XET"
    echo "Phản hồi: $CAN_ACTIONS_GV"
fi

if echo "$CAN_ACTIONS_GV" | grep -q '"canEdit":false'; then
    pass "Giảng viên không được phép sửa đề tài ở trạng thái CHO_PNCKH_XEM_XET"
else
    fail "Giảng viên lẽ ra không được phép sửa ở trạng thái CHO_PNCKH_XEM_XET"
fi

# Kiểm thử với vai trò NCKH01 - Đề tài 1
print_test "RT-01.2: Lấy can-actions của đề tài 1 dưới quyền NCKH01"
CAN_ACTIONS_NCKH=$(curl -s -X GET "$BASE_URL/de-tai/1/can-actions" \
    -H "Authorization: Bearer $NCKH_TOKEN")

if echo "$CAN_ACTIONS_NCKH" | grep -q '"canApprove":true'; then
    pass "Phòng NCKH có quyền duyệt đề tài ở trạng thái CHO_PNCKH_XEM_XET"
else
    fail "Phòng NCKH không có quyền duyệt đề tài"
    echo "Phản hồi: $CAN_ACTIONS_NCKH"
fi

if echo "$CAN_ACTIONS_NCKH" | grep -q '"canRequestSupplementary":true'; then
    pass "Phòng NCKH có quyền yêu cầu bổ sung hồ sơ đề tài"
else
    fail "Phòng NCKH không có quyền yêu cầu bổ sung hồ sơ đề tài"
fi

# Kiểm thử với vai trò PB01 - Đề tài 3 (DANG_PHAN_BIEN, PB01 là thành viên tổ phản biện)
print_test "RT-01.3: Lấy can-actions của đề tài 3 dưới quyền PB01 (phản biện viên được phân công)"
CAN_ACTIONS_PB=$(curl -s -X GET "$BASE_URL/de-tai/3/can-actions" \
    -H "Authorization: Bearer $PB_TOKEN")

if echo "$CAN_ACTIONS_PB" | grep -q '"canSubmitReview":true'; then
    pass "Phản biện viên có quyền nộp phản biện cho đề tài được phân công"
else
    fail "Phản biện viên không có quyền nộp phản biện cho đề tài được phân công"
    echo "Phản hồi: $CAN_ACTIONS_PB"
fi

# Kiểm thử lỗi 403 đối với Phản biện viên truy cập đề tài không được phân công
print_test "RT-01.4: PB01 truy cập đề tài không phân công phản biện phải trả về lỗi 403"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/1/can-actions" \
    -H "Authorization: Bearer $PB_TOKEN")

if [ "$HTTP_CODE" = "403" ]; then
    pass "Hệ thống từ chối quyền truy cập của phản biện viên chính xác (HTTP 403)"
else
    fail "Lẽ ra phải trả về lỗi 403 đối với đề tài không phân công, nhưng nhận được HTTP $HTTP_CODE"
fi

# ============================================================================
# RT-02: Bộ lọc Kỳ NCKH (Ky NCKH Filtering)
# ============================================================================

print_header "3. RT-02: BỘ LỌC KỲ NCKH"

# Kiểm thử với vai trò Giảng viên (GV) - chỉ được xem các kỳ đang mở (active)
print_test "RT-02.1: Giảng viên chỉ lấy được các kỳ NCKH đang mở"
KY_NCKH_GV=$(curl -s -X GET "$BASE_URL/ky-nckh?trangThai=DANG_MO" \
    -H "Authorization: Bearer $GV_TOKEN")

ACTIVE_COUNT=$(echo "$KY_NCKH_GV" | grep -o '"trangThai":"DANG_MO"' | wc -l)
CLOSED_COUNT=$(echo "$KY_NCKH_GV" | grep -o '"trangThai":"DA_DONG"' | wc -l)

if [ "$ACTIVE_COUNT" -eq 1 ] && [ "$CLOSED_COUNT" -eq 0 ]; then
    pass "Giảng viên chỉ nhìn thấy 1 Kỳ NCKH đang hoạt động (lọc chính xác)"
else
    fail "Lọc kỳ cho Giảng viên thất bại: Kỳ mở=$ACTIVE_COUNT, Kỳ đóng=$CLOSED_COUNT"
    echo "Phản hồi: $KY_NCKH_GV"
fi

# Kiểm thử với vai trò Phòng NCKH (NCKH) - được xem tất cả các kỳ
print_test "RT-02.2: Phòng NCKH lấy được tất cả Kỳ NCKH (không lọc)"
KY_NCKH_ALL=$(curl -s -X GET "$BASE_URL/ky-nckh" \
    -H "Authorization: Bearer $NCKH_TOKEN")

TOTAL_COUNT_KY=$(echo "$KY_NCKH_ALL" | grep -o '"id":' | wc -l)

if [ "$TOTAL_COUNT_KY" -eq 2 ]; then
    pass "Phòng NCKH nhìn thấy tất cả 2 Kỳ NCKH (cả đang mở và đã đóng)"
else
    fail "Phòng NCKH lẽ ra phải thấy 2 Kỳ NCKH, nhưng nhận được $TOTAL_COUNT_KY"
    echo "Phản hồi: $KY_NCKH_ALL"
fi

# ============================================================================
# RT-03: Rút Đề tài (Withdrawal)
# ============================================================================

print_header "4. RT-03: RÚT ĐỀ TÀI"

# Kiểm thử rút đề tài hợp lệ - Đề tài 2 (CHO_BO_SUNG_HO_SO)
print_test "RT-03.1: GV01 rút đề tài 2 (trạng thái hợp lệ)"
WITHDRAW_RESPONSE=$(curl -s -X POST "$BASE_URL/de-tai/2/rut-de-tai" \
    -H "Authorization: Bearer $GV_TOKEN" \
    -H "$CONTENT_TYPE")

HTTP_CODE_WITHDRAW=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/de-tai/2/rut-de-tai" \
    -H "Authorization: Bearer $GV_TOKEN" \
    -H "$CONTENT_TYPE")

if [ "$HTTP_CODE_WITHDRAW" = "200" ] || echo "$WITHDRAW_RESPONSE" | grep -q "success"; then
    pass "Rút đề tài 2 thành công"

    # Kiểm tra trạng thái đã chuyển sang DA_RUT
    TOPIC_2_STATUS=$(curl -s -X GET "$BASE_URL/de-tai/2" \
        -H "Authorization: Bearer $GV_TOKEN" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    if [ "$TOPIC_2_STATUS" = "DA_RUT" ]; then
        pass "Trạng thái đề tài 2 đã cập nhật thành DA_RUT"
    else
        fail "Trạng thái đề tài 2 chưa cập nhật, hiện tại là: $TOPIC_2_STATUS"
    fi
else
    fail "Rút đề tài thất bại"
    echo "Phản hồi: $WITHDRAW_RESPONSE"
fi

# Kiểm thử rút đề tài không hợp lệ - Đề tài 7 (DANG_THUC_HIEN - không được phép rút)
print_test "RT-03.2: GV01 cố gắng rút đề tài 7 (trạng thái không được phép)"
HTTP_CODE_INVALID=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/de-tai/7/rut-de-tai" \
    -H "Authorization: Bearer $GV_TOKEN" \
    -H "$CONTENT_TYPE")

if [ "$HTTP_CODE_INVALID" = "400" ]; then
    pass "Hệ thống từ chối rút đề tài chính xác khi ở trạng thái DANG_THUC_HIEN"
else
    fail "Lẽ ra phải từ chối rút đề tài ở trạng thái DANG_THUC_HIEN, nhưng nhận được HTTP $HTTP_CODE_INVALID"
fi

# ============================================================================
# B5-1: Độ hoàn thiện của cấu trúc DeTaiDetailResponse
# ============================================================================

print_header "5. B5-1: PHẢN HỒI CHI TIẾT ĐỀ TÀI"

# Kiểm thử chi tiết đề tài có chứa đầy đủ các trường thông tin bắt buộc
print_test "B5-1.1: Lấy chi tiết đề tài 7 (có đi kèm hợp đồng)"
DETAIL_RESPONSE=$(curl -s -X GET "$BASE_URL/de-tai/7" \
    -H "Authorization: Bearer $GV_TOKEN")

# Kiểm tra các trường cốt lõi
if echo "$DETAIL_RESPONSE" | grep -q '"id":7' && \
   echo "$DETAIL_RESPONSE" | grep -q '"maSo":"NCKH-2026-007"' && \
   echo "$DETAIL_RESPONSE" | grep -q '"tenDeTai"' && \
   echo "$DETAIL_RESPONSE" | grep -q '"status":"DANG_THUC_HIEN"'; then
    pass "Có đầy đủ các trường cốt lõi (id, maSo, tenDeTai, status)"
else
    fail "Thiếu các trường thông tin cốt lõi"
    echo "Phản hồi: $DETAIL_RESPONSE"
fi

# Kiểm tra đối tượng giảng viên chủ nhiệm lồng nhau (nested chuNhiem)
if echo "$DETAIL_RESPONSE" | grep -q '"chuNhiem":{' && \
   echo "$DETAIL_RESPONSE" | grep -q '"hoTen"'; then
    pass "Có đối tượng giảng viên lồng nhau (chuNhiem)"
else
    fail "Thiếu đối tượng chuNhiem hoặc đối tượng bị thiếu trường"
fi

# Kiểm tra đối tượng hợp đồng lồng nhau (nested hopDong)
if echo "$DETAIL_RESPONSE" | grep -q '"hopDong":{' && \
   echo "$DETAIL_RESPONSE" | grep -q '"kinhPhi"'; then
    pass "Có đối tượng hợp đồng lồng nhau (hopDong) đối với đề tài có hợp đồng"
else
    fail "Thiếu đối tượng hợp đồng hopDong"
fi

# Kiểm tra đối tượng hành động khả dụng lồng nhau (nested canActions)
if echo "$DETAIL_RESPONSE" | grep -q '"canActions":{' && \
   echo "$DETAIL_RESPONSE" | grep -q '"canEdit"'; then
    pass "Đối tượng hành động khả dụng (canActions) được nhúng chính xác trong phản hồi"
else
    fail "Thiếu đối tượng canActions trong phản hồi chi tiết đề tài"
fi

# ============================================================================
# A7-1: Xử lý lỗi 403 Forbidden
# ============================================================================

print_header "6. A7-1: XỬ LÝ LỖI 403 FORBIDDEN"

# Giảng viên GV01 cố gắng truy cập đề tài của GV02
print_test "A7-1.1: GV01 cố gắng truy cập đề tài số 11 của GV02"
HTTP_CODE_403=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/11" \
    -H "Authorization: Bearer $GV_TOKEN")

if [ "$HTTP_CODE_403" = "403" ]; then
    pass "Hệ thống từ chối quyền truy cập của GV01 vào đề tài của GV02 chính xác (HTTP 403)"
else
    fail "Lẽ ra phải trả về lỗi 403, nhưng nhận được HTTP $HTTP_CODE_403"
fi

# Phản biện viên cố gắng truy cập đề tài không được phân công phản biện
print_test "A7-1.2: PB01 cố gắng truy cập đề tài số 1 không được phân công"
HTTP_CODE_PB_403=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/1" \
    -H "Authorization: Bearer $PB_TOKEN")

if [ "$HTTP_CODE_PB_403" = "403" ]; then
    pass "Hệ thống từ chối quyền truy cập của PB01 vào đề tài không phân công chính xác (HTTP 403)"
else
    fail "Lẽ ra phải trả về lỗi 403 đối với phản biện viên, nhưng nhận được HTTP $HTTP_CODE_PB_403"
fi

# Phòng NCKH được quyền truy cập tất cả đề tài (không bị lỗi 403)
print_test "A7-1.3: NCKH01 truy cập đề tài bất kỳ (phải thành công)"
HTTP_CODE_NCKH_OK=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/1" \
    -H "Authorization: Bearer $NCKH_TOKEN")

if [ "$HTTP_CODE_NCKH_OK" = "200" ]; then
    pass "Phòng NCKH truy cập đề tài bất kỳ thành công (không bị hạn chế phân quyền)"
else
    fail "Phòng NCKH lẽ ra phải xem được mọi đề tài, nhưng nhận được HTTP $HTTP_CODE_NCKH_OK"
fi

# ============================================================================
# Các kịch bản kiểm thử bổ sung (Additional Tests)
# ============================================================================

print_header "7. XÁC MINH BỔ SUNG"

# Kiểm thử lấy danh sách đề tài
print_test "7.1: Lấy danh sách đề tài dưới quyền GV01"
TOPIC_LIST=$(curl -s -X GET "$BASE_URL/de-tai" \
    -H "Authorization: Bearer $GV_TOKEN")

GV_TOPIC_COUNT=$(echo "$TOPIC_LIST" | grep -o '"id":' | wc -l)

if [ "$GV_TOPIC_COUNT" -ge 1 ]; then
    pass "Giảng viên lấy được danh sách đề tài thành công (có $GV_TOPIC_COUNT đề tài)"
else
    fail "Danh sách đề tài của Giảng viên rỗng hoặc bị lỗi"
fi

# Kiểm thử lỗi 404 cho đề tài không tồn tại
print_test "7.2: Truy cập đề tài không tồn tại ID 9999"
HTTP_CODE_404=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/9999" \
    -H "Authorization: Bearer $GV_TOKEN")

if [ "$HTTP_CODE_404" = "404" ]; then
    pass "Trả về chính xác lỗi 404 cho đề tài không tồn tại"
else
    fail "Lẽ ra phải trả về lỗi 404 cho đề tài không tồn tại, nhưng nhận được HTTP $HTTP_CODE_404"
fi

# Kiểm thử phản biện viên có thể truy cập đề tài được phân công phản biện
print_test "7.3: PB01 truy cập đề tài được phân công phản biện (đề tài 3)"
HTTP_CODE_PB_OK=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/de-tai/3" \
    -H "Authorization: Bearer $PB_TOKEN")

if [ "$HTTP_CODE_PB_OK" = "200" ]; then
    pass "Phản biện viên truy cập thành công đề tài được phân công"
else
    fail "Phản biện viên lẽ ra phải xem được đề tài được phân công, nhưng nhận được HTTP $HTTP_CODE_PB_OK"
fi

# Kiểm thử thời gian phản hồi của hệ thống
print_test "7.4: Kiểm tra thời gian phản hồi của hệ thống (phải dưới 1 giây)"
START_TIME=$(date +%s%N)
curl -s -X GET "$BASE_URL/de-tai/1/can-actions" \
    -H "Authorization: Bearer $NCKH_TOKEN" > /dev/null
END_TIME=$(date +%s%N)
DURATION=$((($END_TIME - $START_TIME) / 1000000)) # Quy đổi sang mili-giây

if [ "$DURATION" -lt 1000 ]; then
    pass "Thời gian phản hồi: ${DURATION}ms (hợp lệ)"
else
    fail "Thời gian phản hồi: ${DURATION}ms (quá chậm)"
fi

# ============================================================================
# Tổng kết Kiểm thử (Test Summary)
# ============================================================================

print_header "TỔNG KẾT KIỂM THỬ"

echo ""
echo -e "Tổng số bài kiểm thử: ${BLUE}$TOTAL_COUNT${NC}"
echo -e "Số bài đạt (Pass):    ${GREEN}$PASS_COUNT${NC}"
echo -e "Số bài lỗi (Fail):    ${RED}$FAIL_COUNT${NC}"
echo ""

if [ "$FAIL_COUNT" -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}   TẤT CẢ CÁC BÀI KIỂM THỬ ĐÃ ĐẠT! ✓${NC}"
    echo -e "${GREEN}========================================${NC}"
    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}   CÓ $FAIL_COUNT BÀI KIỂM THỬ BỊ LỖI! ✗${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
