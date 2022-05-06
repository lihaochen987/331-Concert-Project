package proj.concert.service.mapper;

import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.service.domain.ConcertSummary;

public class ConcertSummaryMapper {
    public static ConcertSummaryDTO toDto(ConcertSummary concertSummary) {
        return new ConcertSummaryDTO(concertSummary.getId(), concertSummary.getTitle(), concertSummary.getImageName());
    }
}
