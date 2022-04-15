package proj.concert.service.mapper;

import proj.concert.service.domain.Concert;
import proj.concert.common.dto.ConcertDTO;

public class ConcertMapper {

    public static Concert toDomainModel(ConcertDTO dtoConcert) {
        return new Concert(dtoConcert.getId(), dtoConcert.getTitle(), dtoConcert.getImageName(), dtoConcert.getBlurb());
    }

    public static ConcertDTO toDto(Concert concert) {
        return new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(), concert.getBlurb());
    }
}
