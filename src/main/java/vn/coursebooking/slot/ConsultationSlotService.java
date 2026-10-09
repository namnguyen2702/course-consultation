package vn.coursebooking.slot;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.booking.BookingRepository;
import vn.coursebooking.consultant.ConsultantRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultationSlotService {

    private final ConsultationSlotRepository slotRepository;
    private final ConsultantRepository consultantRepository;
    private final BookingRepository bookingRepository;

    public ConsultationSlotService(
            ConsultationSlotRepository slotRepository,
            ConsultantRepository consultantRepository,
            BookingRepository bookingRepository
    ) {
        this.slotRepository = slotRepository;
        this.consultantRepository = consultantRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public ConsultationSlot createSlot(CreateSlotRequest request) {
        checkConsultant(request.consultantId());

        LocalDateTime startAt = request.startAt();
        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        if (!startAt.isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giờ bắt đầu phải nằm trong tương lai"
            );
        }

        if ((startAt.getMinute() != 0 && startAt.getMinute() != 30)
                || startAt.getSecond() != 0
                || startAt.getNano() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khung giờ phải bắt đầu vào phút 00 hoặc 30"
            );
        }

        if (slotRepository.existsByConsultantIdAndStartAt(
                request.consultantId(), startAt
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tư vấn viên đã có khung giờ này"
            );
        }

        var consultant = consultantRepository.findById(request.consultantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!consultant.acceptsStartAt(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Khung giờ phải nằm trong ngày và giờ làm việc đã được duyệt");
        }

        ConsultationSlotEntity entity = new ConsultationSlotEntity(
                request.consultantId(),
                startAt
        );

        try {
            ConsultationSlotEntity savedEntity =
                    slotRepository.saveAndFlush(entity);

            return toSlot(savedEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể tạo lịch do xung đột dữ liệu",
                    exception
            );
        }
    }

    public List<ConsultationSlot> getSlots(Long consultantId) {
        checkConsultant(consultantId);

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        List<ConsultationSlotEntity> entities =
                slotRepository
                        .findByConsultantIdAndActiveTrueAndStartAtAfterOrderByStartAtAsc(
                                consultantId, now
                        );

        List<ConsultationSlot> slots = new ArrayList<>();

        for (ConsultationSlotEntity entity : entities) {
            slots.add(toSlot(entity));
        }

        return slots;
    }

    private void checkConsultant(Long consultantId) {
        if (!consultantRepository.existsByIdAndActiveTrue(consultantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy tư vấn viên đang hoạt động"
            );
        }
    }

    private ConsultationSlot toSlot(ConsultationSlotEntity entity) {
        return new ConsultationSlot(
                entity.getId(),
                entity.getConsultantId(),
                entity.getStartAt(),
                entity.getStartAt().plusMinutes(30)
        );
    }
    @Transactional
    public Long deactivateSlot(Long id) {
        ConsultationSlotEntity slot = slotRepository
                .findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy khung giờ"
                ));

        if (bookingRepository.existsBySlotIdAndStatusNot(
                id,
                "CANCELLED"
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể ngừng khung giờ đã có lịch đặt"
            );
        }

        if (!slot.getStartAt().isAfter(
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ ngừng nhận khung giờ trong tương lai"
            );
        }

        slot.deactivate();
        slotRepository.saveAndFlush(slot);

        return slot.getConsultantId();
    }
}