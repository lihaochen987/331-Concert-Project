package proj.concert.service.mapper;

import proj.concert.common.dto.BookingDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Booking;
import proj.concert.service.domain.ConcertSummary;
import proj.concert.service.domain.Seat;

import java.util.ArrayList;

public class BookingMapper {
    public static BookingDTO toDto(Booking booking) {

        ArrayList<SeatDTO> seats = new ArrayList<SeatDTO>();

        if (booking.getSeats() == null){
            return new BookingDTO(booking.getConcertId(), booking.getDate(), new ArrayList<SeatDTO>());
        }

        for (Seat seat: booking.getSeats()){
            seats.add(SeatMapper.toDto(seat));
        }
        return new BookingDTO(booking.getConcertId(), booking.getDate(), seats);
    }
}
