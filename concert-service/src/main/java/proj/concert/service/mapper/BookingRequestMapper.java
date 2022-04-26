package proj.concert.service.mapper;

import proj.concert.common.dto.BookingRequestDTO;
import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.Booking;
import proj.concert.service.domain.BookingRequest;
import proj.concert.service.domain.User;

public class BookingRequestMapper {
    public static BookingRequest toDomainModel(BookingRequestDTO dtoBookingRequest) {
        return new BookingRequest(dtoBookingRequest.getConcertId(), dtoBookingRequest.getDate(), dtoBookingRequest.getSeatLabels());
    }
}
